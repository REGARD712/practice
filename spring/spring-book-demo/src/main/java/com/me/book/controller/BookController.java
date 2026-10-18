package com.me.book.controller;

import com.me.book.model.BookInfo;
import com.me.book.service.BookService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/book")
@RestController
public class BookController {
    /**
     * 图书列表展示接口
     * @param
     * @return List<BookInfo>
     */
    @GetMapping("/getList")
    public List<BookInfo> getList() {
        // 创建service对象
        BookService bookService = new BookService();

        // 调用service层获取图书列表并返回
        return bookService.getBookList();
    }
}
