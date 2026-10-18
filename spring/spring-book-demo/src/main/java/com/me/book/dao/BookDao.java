package com.me.book.dao;

import com.me.book.model.BookInfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BookDao {
    /**
     * 生成模拟数据
     * @param
     * @return List<BookInfo>
     */
    public List<BookInfo> mockData() {
        List<BookInfo> books = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            BookInfo book = new BookInfo();
            book.setId(i);
            book.setBookName("书籍: " + i);
            book.setAuthor("作者: " + i);
            book.setCount(i * 5 + 3);
            book.setPrice(new BigDecimal(new Random().nextInt(100)));
            book.setPublish("出版社: " + i);
            book.setStatus(i % 2 == 0 ? 1 : 2);
            books.add(book);
        }
        return books;
    }
}
