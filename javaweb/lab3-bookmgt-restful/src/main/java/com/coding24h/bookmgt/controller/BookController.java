package com.coding24h.bookmgt.controller;

import com.coding24h.bookmgt.entity.Book;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private List<Book> books = new ArrayList<>();
    private final AtomicLong counter = new AtomicLong();

    public BookController() {
        books.add(new Book(counter.incrementAndGet(), "Spring Boot从入门到精通", "张三"));
        books.add(new Book(counter.incrementAndGet(), "高并发架构实战：从需求分析到系统设计",
                "李四"));
    }

    // GET /api/books - 查询所有图书
    @GetMapping
    public List<Book> getAllBooks() {
        return books;
    }

    // GET /api/books/{id} - 根据ID查询单个图书
    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Long id) {
        return books.stream()
                .filter(book -> book.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // POST /api/books - 创建新图书
    @PostMapping
    public Book createBook(@RequestBody Book book) {
        book.setId(counter.incrementAndGet()); // 生成ID
        books.add(book);
        return book;
    }

    // PUT /api/books/{id} - 更新图书信息
    @PutMapping("/{id}")
    public Book updateBook(@PathVariable Long id, @RequestBody Book bookDetails) {
        Book book = books.stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (book != null) {
            book.setTitle(bookDetails.getTitle());
            book.setAuthor(bookDetails.getAuthor());
            return book;
        }
        return null;
    }

    // DELETE /api/books/{id} - 删除图书
    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        books.removeIf(book -> book.getId().equals(id));
    }
}
