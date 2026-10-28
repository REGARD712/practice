package com.coding24h.itnews.service;

import com.coding24h.itnews.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> findAll();

    Category findById(Long id);

    void add(Category category);

    void update(Category category);

    void delete(Long id);
}
