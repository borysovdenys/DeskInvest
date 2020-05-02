package com.borysov.dev.controllers;

import com.borysov.dev.models.User;
import com.borysov.dev.services.MailingService;
import com.borysov.dev.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Objects;

@Controller
public class UserController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    @Autowired
    private MailingService mailingService;

    @GetMapping(value = "index")
    public String getLoginPage(Model model, @RequestParam(required = false) String error, Principal principal, @AuthenticationPrincipal User user) {
        if (error != null) {
            model.addAttribute("loginError", true);
        }

        return Objects.nonNull(principal) ? "redirect:home" : "index";
    }

    @GetMapping(value = "/home")
    public String getHomePage(@AuthenticationPrincipal User user, HttpServletRequest request) {
        if (user != null) {
            request.getSession().setAttribute("userName", user.getFirstName() + " " + user.getLastName());
        }
        return "home";
    }

    @PostMapping(value = "/home")
    public String getHomePagePost() {
        return "redirect:home";
    }
}
