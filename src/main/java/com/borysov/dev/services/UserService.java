package com.borysov.dev.services;

import com.borysov.dev.models.User;

import java.util.List;

public interface UserService {

    User findUserByEmail(String email);

    List<User> getAll();
}
