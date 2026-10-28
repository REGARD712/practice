package com.coding24h.itnews.service;

import com.coding24h.itnews.entity.User;
import java.util.List;

public interface UserService {
    List<User> findAll();

    User findById(Long id);
}