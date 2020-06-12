package com.borysov.dev.services.impl;

import com.borysov.dev.dtos.ItemDto;
import com.borysov.dev.helpers.RequestHelper;
import com.borysov.dev.mappers.ItemDtoMapper;
import com.borysov.dev.models.Currency;
import com.borysov.dev.models.Item;
import com.borysov.dev.models.Price;
import com.borysov.dev.models.User;
import com.borysov.dev.models.enums.CurrencyEnum;
import com.borysov.dev.models.enums.ItemType;
import com.borysov.dev.properties.CommonProperties;
import com.borysov.dev.repositories.CurrencyRepository;
import com.borysov.dev.repositories.ItemRepository;
import com.borysov.dev.repositories.PriceRepository;
import com.borysov.dev.services.ItemService;
import com.borysov.dev.services.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
@Log4j
@RequiredArgsConstructor
@Data
public class ItemServiceImpl implements ItemService {

    private final UserService userService;
    private final ItemRepository itemRepository;
    private final ItemDtoMapper itemDtoMapper;
    private final CurrencyRepository currencyRepository;
    private final PriceRepository priceRepository;
    private final EntityManager entityManager;

    private final static String REQUEST_URL_INFO = "https://steamcommunity.com/market/priceoverview/?market_hash_name=%s&appid=%s&currency=1";
    private final static String REQUEST_URL_IMAGE = "https://steamcommunity.com/market/listings/%s/%s/render?start=0&count=1&currency=1&format=json";

    private Element documentBody;
    private ItemType itemType;


    @Override
    public ItemDto findAndPrepareItem(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            return ItemDto.builder().startDateTrack(LocalDate.now()).build();
        }

        return itemRepository.findByUuid(uuid).map(itemDtoMapper::toDto).orElse(new ItemDto());
    }

    @Override
    public boolean updateItem(ItemDto modifiedItemDto, User user) throws IOException {

        try {
            if (modifiedItemDto.getUrl().contains(CommonProperties.SOTA_PART_URL)) {
                setItemType(ItemType.SOTA);
                setDocumentBody(RequestHelper.getDocumentByHTML(modifiedItemDto.getUrl()).body());
                return fillItem(modifiedItemDto, user);
            } else if (modifiedItemDto.getUrl().contains(CommonProperties.STEAM_PART_URL)) {
                setItemType(ItemType.STEAM);
                return fillItem(modifiedItemDto, user);
            }
        } catch (Exception e) {
            log.error(e);
        }

        return false;
    }

    private boolean fillItem(ItemDto modifiedItemDto, User user) throws IOException {

        Item item = null;

        try {

            Pair<CurrencyEnum, BigDecimal> currencyPriceCurrent = getCurrentCurrencyPrice(modifiedItemDto.getUrl());

            deleteOldPrices(modifiedItemDto);

            item = itemRepository.findByUuid(modifiedItemDto.getUuid()).orElse(new Item());

            item.setItemType(getItemType());
            item.setUser(user);
            item.setUrl(modifiedItemDto.getUrl());
            item.setName(getItemName(modifiedItemDto.getUrl()));
            item.setPicture(getItemPicture(modifiedItemDto.getUrl()));
            item.setStartDateTrack(LocalDateTime.of(modifiedItemDto.getStartDateTrack(), LocalTime.now()));
            item.setLastDateUpdate(LocalDateTime.now());

            itemRepository.save(item);

            generateAndSaveItemPrices(modifiedItemDto, item, currencyPriceCurrent);
            return true;
        } catch (Exception e) {
            log.error(e);
            if (item != null && item.getPrices().isEmpty()) {
                itemRepository.delete(item);
            }
            return false;
        }
    }

    private Pair<CurrencyEnum, BigDecimal> getCurrentCurrencyPrice(String url) throws IOException {
        String price = "";
        CurrencyEnum currency = null;
        if (ItemType.SOTA.equals(getItemType())) {
            price = getDocumentBody().getElementsByClass("dropdown-item").attr("data-price_full");
            currency = CurrencyEnum.UAH;
        } else if (ItemType.STEAM.equals(getItemType())) {
            price = getItemPrice(url).trim().substring(1);
            currency = CurrencyEnum.USD;
        }
        return Pair.of(currency, new BigDecimal(price));
    }

    private BigDecimal getCurrentPriceFromMarketDecimal(String itemPrice) {
        return new BigDecimal(itemPrice.trim().substring(1));
    }

    private void deleteOldPrices(ItemDto modifiedItemDto) {
        if (modifiedItemDto.getUuid() != null) {
            Optional<Item> item = itemRepository.findByUuid(modifiedItemDto.getUuid());
            item.ifPresent(i -> priceRepository.deleteAll(i.getPrices()));
        }
    }

    @Override
    public Page<Item> getAll(Pageable pageable) {
        return itemRepository.findAll(pageable);
    }

    @Override
    public Page<Item> getAllByUserUUID(Pageable pageable, UUID uuid) {
        entityManager.clear();
        return itemRepository.findAllByUserUuid(pageable, uuid);
    }

    @Override
    public boolean updateItemsByUserUUID(UUID currentAuditorUUID) {
        List<Currency> latestCurrencies = currencyRepository.getLatestCurrencies(CurrencyEnum.values().length);
        List<Item> allItemsUserUuid = itemRepository.findAllByUserUuid(currentAuditorUUID);

        Map<CurrencyEnum, BigDecimal> mapRates = getMapRates(latestCurrencies);

        try {
            for (Item item : allItemsUserUuid) {
                BigDecimal currentPriceFromMarket = getCurrentPriceFromMarketDecimal(getItemPrice(item.getUrl()));
                for (Price price : item.getPrices()) {
                    price.setCurrentRate(mapRates.get(price.getAbbreviation()).multiply(currentPriceFromMarket).setScale(2, RoundingMode.HALF_UP));
                    priceRepository.save(price);
                }
                item.setLastDateUpdate(LocalDateTime.now());
                itemRepository.save(item);
            }
            return true;
        } catch (Exception e) {
            log.error(e);
            return false;
        }
    }

    @Override
    public void deleteItemsByUserUUID(UUID currentAuditorUUID) {
        itemRepository.deleteAll(itemRepository.findAllByUserUuid(currentAuditorUUID));
    }

    private Map<CurrencyEnum, BigDecimal> getMapRates(List<Currency> latestCurrencies) {
        Map<CurrencyEnum, BigDecimal> result = new HashMap();

        for (CurrencyEnum value : CurrencyEnum.values()) {
            result.put(value, getRate(latestCurrencies, value));
        }
        return result;
    }

    @Override
    public Item findItemByUUID(UUID uuid) {
        return itemRepository.findByUuid(uuid).orElse(null);
    }

    @Override
    public void deleteItemByUUID(UUID uuid) {
        try {
            itemRepository.deleteByUuid(uuid);
        } catch (Exception e) {
            log.error(e);
        }
    }

    private void generateAndSaveItemPrices(ItemDto modifiedItemDto, Item item, final Pair<CurrencyEnum, BigDecimal> currentPriceFromMarket) throws IOException {
        Price price;
        BigDecimal rateInSelectedCurrency;
        List<Currency> latestCurrencies = currencyRepository.getLatestCurrencies(CurrencyEnum.values().length);

        Map<CurrencyEnum, BigDecimal> mapRates = getMapRates(latestCurrencies);

        for (CurrencyEnum currencyEnumValue : CurrencyEnum.values()) {
            price = new Price(currencyEnumValue);
            price.setItem(item);

            if (currencyEnumValue.equals(modifiedItemDto.getStartCurrency())) {
                price.setStartRate(modifiedItemDto.getStartPrice().setScale(2, RoundingMode.HALF_UP));
            } else {
                rateInSelectedCurrency = getExchange(modifiedItemDto.getStartPrice(), modifiedItemDto.getStartCurrency(), mapRates, currencyEnumValue);
                price.setStartRate(modifiedItemDto.getStartPrice().divide(rateInSelectedCurrency, 2, RoundingMode.HALF_UP));
            }

            rateInSelectedCurrency = getExchange(currentPriceFromMarket.getRight(), currentPriceFromMarket.getLeft(), mapRates, currencyEnumValue);
            price.setCurrentRate(currentPriceFromMarket.getRight().divide(rateInSelectedCurrency, 2, RoundingMode.HALF_UP));

            priceRepository.save(price);
        }
    }

    private BigDecimal getExchange(BigDecimal value, CurrencyEnum startCurrency,
                                   Map<CurrencyEnum, BigDecimal> mapRates, CurrencyEnum currencyEnumValue) {
        if (BigDecimal.ZERO.compareTo(value) < 0) {
            BigDecimal rateInSelectedCurrency = mapRates.get(startCurrency);
            BigDecimal rateInCurrentCurrency = mapRates.get(currencyEnumValue);
            return rateInSelectedCurrency.divide(rateInCurrentCurrency, 5, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    @NotNull
    private BigDecimal getRate(List<Currency> latestCurrencies, CurrencyEnum currencyEnumValue) {
        return latestCurrencies.stream()
                .filter(x -> currencyEnumValue.equals(x.getAbbreviation()))
                .map(Currency::getRate)
                .findFirst().orElse(BigDecimal.ZERO);
    }

    private String getItemPrice(String url) throws IOException {
        JSONObject jsonObject;
        String[] parts = url.split("/");
        String gameId = parts[5];
        String marketHashName = parts[6];

        try {
            jsonObject = RequestHelper.readJsonFromUrl(String.format(REQUEST_URL_INFO, marketHashName, gameId));
            return (String) jsonObject.get("lowest_price");
        } catch (IOException | JSONException e) {
            log.error("NPE from steam");
            return "0";
        }
    }

    private String getItemPicture(String url) throws IOException {

        if (ItemType.SOTA.equals(getItemType())) {
            return getDocumentBody().getElementById("image").attr("src");
        } else if (ItemType.STEAM.equals(getItemType())) {

            String[] parts = url.split("/");
            String gameId = parts[5];
            String marketHashName = parts[6];

            JSONObject jsonObject;
            jsonObject = RequestHelper.readJsonFromUrl(String.format(REQUEST_URL_IMAGE, gameId, marketHashName));

            Pattern p = Pattern.compile("src=\"(.*?)\"");
            Matcher m = p.matcher(jsonObject.get("results_html").toString());
            if (m.find()) {
                return m.group(1);
            }
        }
        return null;
    }

    private String getItemName(String url) throws UnsupportedEncodingException {

        if (ItemType.SOTA.equals(getItemType())) {
            return getDocumentBody().getElementById("image").attr("alt");
        } else if (ItemType.STEAM.equals(getItemType())) {
            String[] parts = url.split("/");

            return URLDecoder.decode(parts[6], StandardCharsets.UTF_8.name());
        }
        return "";
    }

    public boolean updateAllSotaItems() {
        boolean update = false;
        try {
            List<User> users = userService.getAll();
            for (User user : users) {
                List<Item> sotaItems = user.getItems().stream().filter(Item::isSotaItem).collect(Collectors.toList());

                for (Item item : sotaItems) {
                    ItemDto itemDto = itemDtoMapper.toDto(item);
                    Thread.sleep(10000);
                    update = updateItem(itemDto, user);

                    if (update) {
                        log.info(item.getName() + " updated");
                    } else {
                        log.error(item.getName() + " update failed");
                    }
                }
            }
            return update;
        } catch (Exception e) {
            return false;
        }
    }
}
