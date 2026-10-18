package com.me.book.service;

import com.me.book.dao.BookDao;
import com.me.book.model.BookInfo;

import java.util.List;

public class BookService {
    public List<BookInfo> getBookList() {
        // 创建dao对象
        BookDao bookDao = new BookDao();

        //获取数据
        List<BookInfo> books = bookDao.mockData();

        //处理页面展示
        for (BookInfo book : books) {
            if (book.getStatus() == 1) {
                book.setStatusCN("可借阅");
            } else {
                book.setStatusCN("不可借阅");
            }
        }

        // 返回数据
        return books;
    }
}
