package com.coding24h.imessage.service;

import com.coding24h.imessage.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAll();
    Category getById(Long id);
}
