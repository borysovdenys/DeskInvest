package com.borysov.dev.services.impl;

import com.borysov.dev.models.User;
import com.borysov.dev.repositories.UserRepository;
import com.borysov.dev.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private MailingServiceImpl mailingService;

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(new User());
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

}
