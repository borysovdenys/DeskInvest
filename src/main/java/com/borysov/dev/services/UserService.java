package com.borysov.dev.services;

import com.borysov.dev.models.User;

import javax.validation.Valid;

public interface UserService {

    User findUserByEmail(String email);

}
