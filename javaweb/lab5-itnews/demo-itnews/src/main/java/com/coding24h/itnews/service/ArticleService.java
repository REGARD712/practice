package com.coding24h.itnews.service;

import com.coding24h.itnews.entity.Article;
import java.util.List;

public interface ArticleService {
    List<Article> findAll();

    List<Article> findByCategoryId(Long categoryId);

    Article findById(Long id);

    void add(Article article);

    void update(Article article);

    void delete(Long id);
}
