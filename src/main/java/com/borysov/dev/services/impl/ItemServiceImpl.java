package com.borysov.dev.services.impl;

import com.borysov.dev.dtos.ItemDto;
import com.borysov.dev.helpers.RequestHelper;
import com.borysov.dev.mappers.ItemDtoMapper;
import com.borysov.dev.models.Currency;
import com.borysov.dev.models.Item;
import com.borysov.dev.models.Price;
import com.borysov.dev.models.User;
import com.borysov.dev.models.enums.CurrencyEnum;
import com.borysov.dev.repositories.CurrencyRepository;
import com.borysov.dev.repositories.ItemRepository;
import com.borysov.dev.repositories.PriceRepository;
import com.borysov.dev.services.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.io.IOException;
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

@Service
@Transactional
@Log4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemDtoMapper itemDtoMapper;
    private final CurrencyRepository currencyRepository;
    private final PriceRepository priceRepository;

    private final static String REQUEST_URL_INFO = "https://steamcommunity.com/market/priceoverview/?market_hash_name=%s&appid=%s&currency=1";
    private final static String REQUEST_URL_IMAGE = "https://steamcommunity.com/market/listings/%s/%s/render?start=0&count=1&currency=1&format=json";


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
    public void updateItem(ItemDto modifiedItemDto, User user) throws IOException {

        deleteOldPrices(modifiedItemDto);

        Item item = itemRepository.findByUuid(modifiedItemDto.getUuid()).orElse(new Item());

        item.setUser(user);
        item.setUrl(modifiedItemDto.getUrl());
        item.setName(URLDecoder.decode(getItemName(modifiedItemDto.getUrl()), StandardCharsets.UTF_8.name()));
        item.setPicture(getItemPicture(modifiedItemDto.getUrl()));
        item.setStartDateTrack(LocalDateTime.of(modifiedItemDto.getStartDateTrack(), LocalTime.now()));
        item.setLastDateUpdate(LocalDateTime.now());

        itemRepository.save(item);

        generateAndSaveItemPrices(modifiedItemDto, item);
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
        return itemRepository.findAllByUserUuid(pageable, uuid);
    }

    @Override
    public void updateItemsByUserUUID(UUID currentAuditorUUID) throws IOException {
        List<Currency> latestCurrencies = currencyRepository.getLatestCurrencies(CurrencyEnum.values().length);
        List<Item> allItemsUserUuid = itemRepository.findAllByUserUuid(currentAuditorUUID);

        Map<CurrencyEnum, BigDecimal> mapRates = getMapRates(latestCurrencies);

        for (Item item : allItemsUserUuid) {
            BigDecimal currentPriceFromMarket = getCurrentPriceFromMarketDecimal(getItemPrice(item.getUrl()));
            for (Price price : item.getPrices()) {
                price.setCurrentRate(mapRates.get(price.getAbbreviation()).multiply(currentPriceFromMarket).setScale(2, RoundingMode.HALF_UP));
                priceRepository.save(price);
            }
            item.setLastDateUpdate(LocalDateTime.now());
            itemRepository.save(item);
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

    private void generateAndSaveItemPrices(ItemDto modifiedItemDto, Item item) throws IOException {
        final BigDecimal currentPriceFromMarket = getCurrentPriceFromMarketDecimal(getItemPrice(modifiedItemDto.getUrl()));
        BigDecimal rate;
        Price price;

        List<Currency> latestCurrencies = currencyRepository.getLatestCurrencies(CurrencyEnum.values().length);

        for (CurrencyEnum currencyEnumValue : CurrencyEnum.values()) {
            price = new Price(currencyEnumValue);
            price.setItem(item);
            rate = getRate(latestCurrencies, currencyEnumValue);

            price.setStartRate(rate.multiply(modifiedItemDto.getStartPriceUSD()));
            price.setCurrentRate(rate.multiply(currentPriceFromMarket));
            priceRepository.save(price);
        }

    }

    private BigDecimal getCurrentPriceFromMarketDecimal(String itemPrice) {
        return new BigDecimal(itemPrice.trim().substring(1));
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
        return null;
    }

    private String getItemName(String url) {
        String[] parts = url.split("/");
        return parts[6];
    }
}
