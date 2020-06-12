package com.borysov.dev.services.impl;

import com.borysov.dev.models.Item;
import com.borysov.dev.models.Price;
import com.borysov.dev.models.User;
import com.borysov.dev.models.enums.CurrencyEnum;
import com.borysov.dev.services.ItemService;
import com.borysov.dev.services.MailingService;
import com.borysov.dev.services.NotificationService;
import com.borysov.dev.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final MailingService mailingService;
    private final ItemService itemService;
    private final UserService userService;
    private final EntityManager entityManager;

    @Scheduled(cron = "0 25 19 * * *")
    public void scheduleCheckCurrentPriceLowerThanStartPrice() {
        boolean isUpdated = itemService.updateAllSotaItems();
        entityManager.clear();
        if(isUpdated) {
            List<User> users = userService.getAll();
            checkPricesAndNotify(users);
        }
    }

    private void checkPricesAndNotify(List<User> users) {
        for (User user : users) {
            List<Item> sotaItems = user.getItems().stream().filter(Item::isSotaItem).collect(Collectors.toList());

            for (Item item : sotaItems) {
                Price price = item.getPrices().stream().filter(x -> CurrencyEnum.UAH.equals(x.getAbbreviation())).findFirst().orElse(null);
                if (price != null && price.getStartRate().compareTo(price.getCurrentRate()) > 0) {

                    String text = String.format("Hi! Good news for you!\n" +
                            "Price for %s is lower than you expected!\n" +
                            "It costs %s %s!\n" +
                            "Check it by yourself!\n" +
                            "%s", item.getName(), price.getCurrentRate(), price.getAbbreviation(), item.getUrl());
                    mailingService.sendMail(user.getEmail(), text);

                }
            }
        }
    }

}
