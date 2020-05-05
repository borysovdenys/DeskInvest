package com.borysov.dev.services;

import com.borysov.dev.models.User;

public interface UserService {

    User findUserByEmail(String email);

}
