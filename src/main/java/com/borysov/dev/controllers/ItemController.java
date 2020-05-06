package com.borysov.dev.controllers;

import com.borysov.dev.configurations.auditable.JpaAuditingConfiguration;
import com.borysov.dev.constants.Urls;
import com.borysov.dev.dtos.ItemDto;
import com.borysov.dev.models.User;
import com.borysov.dev.services.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Controller
@Log4j
@RequiredArgsConstructor
@RequestMapping(Urls.Item.FULL)
public class ItemController {

    private final ItemService itemService;
    private final JpaAuditingConfiguration jpaAuditingConfiguration;

    @GetMapping
    public String getItemsPage(@AuthenticationPrincipal User user, @PageableDefault(size = 5, sort = {"createdDate"}) Pageable pageable, Model model) {
        model.addAttribute("page", itemService.getAllByUserUUID(pageable, jpaAuditingConfiguration.getCurrentAuditorUUID()));
        return Objects.nonNull(user) ? "items" : "index";
    }

    @GetMapping(Urls.Item.ID.PART)
    public String handleEditItem(@PathVariable String uuid, @PageableDefault(size = 5, sort = {"createdDate"}) Pageable pageable, Model model) {
        model.addAttribute("page", itemService.getAllByUserUUID(pageable, jpaAuditingConfiguration.getCurrentAuditorUUID()));
        model.addAttribute("takenItem", itemService.findAndPrepareItem(uuid));
        return "items";
    }

    @PostMapping(Urls.Item.SaveItem.PART)
    public String saveItem(@AuthenticationPrincipal User user, @PageableDefault(size = 5, sort = {"createdDate"}) Pageable pageable,
                           @Valid ItemDto modifiedItemDto, Model model) {
        try {
            itemService.updateItem(modifiedItemDto, user);
        } catch (IOException e) {
            model.addAttribute("page", itemService.getAllByUserUUID(pageable, jpaAuditingConfiguration.getCurrentAuditorUUID()));
            model.addAttribute("addItemError", true);
            return "items";
        }
        return "redirect:";
    }


    @GetMapping(Urls.Item.DeleteItem.FULL)
    public String getDeleteItem(@PathVariable UUID uuid, Model model,
                                @PageableDefault(size = 5, sort = {"createdDate"}) Pageable pageable) {
        model.addAttribute("deleteItem", itemService.findItemByUUID(uuid));
        model.addAttribute("page", itemService.getAllByUserUUID(pageable, jpaAuditingConfiguration.getCurrentAuditorUUID()));
        return "items";
    }

    @GetMapping(Urls.Item.DeleteItem.AlL)
    public String deleteAll(Model model, @PageableDefault(size = 5, sort = {"createdDate"}) Pageable pageable) {
        itemService.deleteItemsByUserUUID(jpaAuditingConfiguration.getCurrentAuditorUUID());
        model.addAttribute("page", itemService.getAllByUserUUID(pageable, jpaAuditingConfiguration.getCurrentAuditorUUID()));
        return "items";
    }

    @PostMapping(Urls.Item.DeleteItem.PART)
    public String handleDeleteItem(@PageableDefault(size = 5, sort = {"createdDate"}) Pageable pageable, Model model, String uuid) {
        itemService.deleteItemByUUID(UUID.fromString(uuid));
        model.addAttribute("page", itemService.getAllByUserUUID(pageable, jpaAuditingConfiguration.getCurrentAuditorUUID()));
        return "items";
    }

    @GetMapping(Urls.Item.UpdateAll.PART)
    public String update(@AuthenticationPrincipal User user, Model model,
                         @PageableDefault(size = 5, sort = {"createdDate"}) Pageable pageable) throws IOException {
        itemService.updateItemsByUserUUID(jpaAuditingConfiguration.getCurrentAuditorUUID());
        model.addAttribute("page", itemService.getAllByUserUUID(pageable, jpaAuditingConfiguration.getCurrentAuditorUUID()));
        return "items";
    }
}
