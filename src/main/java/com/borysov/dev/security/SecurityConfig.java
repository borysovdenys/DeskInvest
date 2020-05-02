package com.borysov.dev.security;

import com.borysov.dev.models.Role;
import com.borysov.dev.models.User;
import com.borysov.dev.repositories.RoleRepository;
import com.borysov.dev.repositories.UserRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableOAuth2Sso
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private static final Logger LOGGER = Logger.getLogger(SecurityConfig.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/", "/index", "/**/*.css", "/static/**", "/**/*.js")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin().loginPage("/index").permitAll()
                .successForwardUrl("/home")
                .and()
                .logout().permitAll();
    }

    @Bean
    public PrincipalExtractor principalExtractor(UserRepository userRepository, HttpServletRequest request) {
        return map -> {
            String email = String.valueOf(map.get("email"));
            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setFirstName(String.valueOf(map.get("given_name")));
                newUser.setLastName(String.valueOf(map.get("family_name")));
                newUser.setEmail(String.valueOf(map.get("email")));
                newUser.setPicture(String.valueOf(map.get("picture")));
                newUser.setLocale(String.valueOf(map.get("locale")));


                newUser.setAuthorizedWithGoogle(true);
                Role userRole = roleRepository.findByName("user");
                //newUser.setRoles(Arrays.asList(userRole));
                return newUser;
            });

            request.getSession().setAttribute("authorizedWithGoogle", true);

            return userRepository.save(user);
        };
    }
}
