package com.coding24h.itnews.service.impl;

import com.coding24h.itnews.entity.Category;
import com.coding24h.itnews.mapper.CategoryMapper;
import com.coding24h.itnews.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<Category> findAll() {
        return categoryMapper.selectAll();
    }

    @Override
    public Category findById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    @Transactional
    public void add(Category category) {
        categoryMapper.insert(category);
    }

    @Override
    @Transactional
    public void update(Category category) {
        categoryMapper.update(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        categoryMapper.deleteById(id);
    }
}
