package com.coding24h.itnews.service.impl;

import com.coding24h.itnews.entity.Article;
import com.coding24h.itnews.mapper.ArticleMapper;
import com.coding24h.itnews.service.ArticleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {

    /** 依赖通过构造器注入，保证不可变并在启动阶段暴露装配问题 */
    private final ArticleMapper articleMapper;

    public ArticleServiceImpl(ArticleMapper articleMapper) {
        this.articleMapper = articleMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Article> findAll() {
        return articleMapper.selectAllWithUserAndCategory();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Article> findByCategoryId(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        return articleMapper.selectByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public Article findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        return articleMapper.selectById(id);
    }

    @Override
    @Transactional
    public void add(Article article) {
        validateContent(article);
        // user_id / category_id 为表非空外键，提前校验可避免数据库层抛出难以定位的异常
        if (article.getUserId() == null) {
            throw new IllegalArgumentException("文章作者不能为空");
        }
        if (article.getCategoryId() == null) {
            throw new IllegalArgumentException("文章分类不能为空");
        }
        // 插入失败时向外抛出异常，避免上层误以为写入成功
        if (articleMapper.insert(article) != 1) {
            throw new IllegalStateException("新增文章失败");
        }
    }

    @Override
    @Transactional
    public void update(Article article) {
        validateContent(article);
        if (article.getId() == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        int rows = articleMapper.update(article);
        // MySQL 默认返回“实际变更行数”，提交相同数据时同样为 0，需回查区分“记录不存在”与“无字段变化”
        if (rows == 0 && articleMapper.selectById(article.getId()) == null) {
            throw new IllegalStateException("更新失败，文章不存在：id=" + article.getId());
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        if (articleMapper.deleteById(id) != 1) {
            throw new IllegalStateException("删除失败，文章不存在：id=" + id);
        }
    }

    /**
     * 校验并规整文章内容，避免空标题、纯空白内容写入数据库
     */
    private void validateContent(Article article) {
        if (article == null) {
            throw new IllegalArgumentException("文章对象不能为空");
        }
        String title = article.getTitle();
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("文章标题不能为空");
        }
        article.setTitle(title.trim());
        String content = article.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("文章内容不能为空");
        }
        article.setContent(content.trim());
    }
}
