package com.coding24h.itnews.controller;

import com.coding24h.itnews.entity.Article;
import com.coding24h.itnews.service.ArticleService;
import com.coding24h.itnews.service.CategoryService;
import com.coding24h.itnews.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ArticleController {

    /** 依赖通过构造器注入，保证不可变并在启动阶段暴露装配问题 */
    private final ArticleService articleService;
    private final CategoryService categoryService;
    private final UserService userService;

    public ArticleController(ArticleService articleService,
                             CategoryService categoryService,
                             UserService userService) {
        this.articleService = articleService;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    // 首页（支持按分类筛选）
    @GetMapping("/")
    public String home(@RequestParam(required = false) Long categoryId, Model model) {
        if (categoryId != null) {
            model.addAttribute("articles",
                    articleService.findByCategoryId(categoryId));
        } else {
            model.addAttribute("articles", articleService.findAll());
        }
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("selectedCategoryId", categoryId);
        return "home";
    }

    // 新闻详情
    @GetMapping("/article/{id}")
    public String view(@PathVariable Long id, Model model) {
        Article article = articleService.findById(id);
        // 文章不存在时返回 404，避免模板拿到 null 后抛出 500
        if (article == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "文章不存在：id=" + id);
        }
        model.addAttribute("article", article);
        return "view";
    }

    // ---------- 新闻管理（增删改查）----------
    @GetMapping("/admin/articles")
    public String adminList(Model model) {
        model.addAttribute("articles", articleService.findAll());
        return "admin/article_list";
    }

    @GetMapping("/admin/articles/add")
    public String addForm(Model model) {
        model.addAttribute("article", new Article());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("users", userService.findAll());
        return "admin/article_form";
    }

    @PostMapping("/admin/articles/save")
    public String save(@ModelAttribute Article article, Model model) {
        // 必填项缺失时回填表单，避免把用户输入直接丢给数据库触发非空约束（500）
        boolean blankTitle = article.getTitle() == null || article.getTitle().trim().isEmpty();
        boolean blankContent = article.getContent() == null || article.getContent().trim().isEmpty();
        if (blankTitle || blankContent || article.getUserId() == null || article.getCategoryId() == null) {
            model.addAttribute("article", article);
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("users", userService.findAll());
            model.addAttribute("errorMessage", "标题、内容、作者和分类均不能为空");
            return "admin/article_form";
        }
        if (article.getId() == null) {
            articleService.add(article);
        } else {
            articleService.update(article);
        }
        return "redirect:/admin/articles";
    }

    @GetMapping("/admin/articles/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Article article = articleService.findById(id);
        // 记录不存在时返回 404，否则表单绑定 null 对象会抛异常
        if (article == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "文章不存在：id=" + id);
        }
        model.addAttribute("article", article);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("users", userService.findAll());
        return "admin/article_form";
    }

    /**
     * 删除使用 POST：GET 请求可能被浏览器预取、爬虫或第三方页面（如 img src）触发，导致数据被误删。
     */
    @PostMapping("/admin/articles/delete/{id}")
    public String delete(@PathVariable Long id) {
        articleService.delete(id);
        return "redirect:/admin/articles";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
