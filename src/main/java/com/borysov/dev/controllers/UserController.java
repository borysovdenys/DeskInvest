package com.borysov.dev.controllers;

import com.borysov.dev.constants.Urls;
import com.borysov.dev.models.User;
import com.borysov.dev.services.MailingService;
import com.borysov.dev.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Log4j
@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;
    private final MailingService mailingService;

    @GetMapping({Urls.ROOT, Urls.User.Index.FULL})
    public String getPage(@AuthenticationPrincipal User user) {
        return Objects.nonNull(user) ? "redirect:home" : "index";
    }

    @GetMapping(Urls.User.Home.FULL)
    public String getHomePage(@AuthenticationPrincipal User user, HttpServletRequest request) {
        if (user != null) {
            request.getSession().setAttribute("userName", user.getFirstName() + " " + user.getLastName());
        }
        return "home";
    }

    @PostMapping(Urls.User.Home.FULL)
    public String getHomePagePost() {
        return "redirect:home";
    }

/*    @RequestMapping("/user")
    public User user(OAuth2Authentication authentication) {
        User u = (User) authentication.getPrincipal();
        u.getEmail()
        LinkedHashMap<String, Object> properties = (LinkedHashMap<String, Object>) authentication.getUserAuthentication().getDetails();
       // return properties.get("email");
        return "22";
    }*/
}
