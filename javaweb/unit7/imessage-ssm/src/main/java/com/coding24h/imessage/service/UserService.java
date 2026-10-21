package com.coding24h.imessage.service;

import com.coding24h.imessage.entity.User;
import java.util.List;

public interface UserService {
    List<User> getAll();
    User getById(Long id);
}