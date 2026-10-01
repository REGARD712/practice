package com.coding24h.bookmgt.controller;

import com.coding24h.bookmgt.entity.Book;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Controller
public class BookController {
    private List<Book> books = new ArrayList<>();
    private final AtomicLong counter = new AtomicLong();

    public BookController() {
        books.add(new Book(counter.incrementAndGet(), "Spring Boot从入门到精通", "张三"));
        books.add(new Book(counter.incrementAndGet(), "高并发架构实战：从需求分析到系统设计", "李四"));
    }

    @GetMapping("/")
    public String listBooks(Model model) {
        model.addAttribute("books", books);
        model.addAttribute("book", new Book());
        return "books";
    }

    @PostMapping("/book")
    public String addOrUpdateBook(@ModelAttribute Book book) {
        if (book.getId() == null) {
            book.setId(counter.incrementAndGet());
            books.add(book);
        } else {
            Optional<Book> bookToUpdate = books.stream().filter(b ->
                    b.getId().equals(book.getId())).findFirst();
            bookToUpdate.ifPresent(b -> {
                b.setTitle(book.getTitle());
                b.setAuthor(book.getAuthor());
            });
        }
        return "redirect:/";
    }

    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        return null;
    }

    @GetMapping("/edit/{id}")
    public String editBook(@PathVariable Long id, Model model) {
    //2.编辑图书代码需你自行完成
        return null;
    }
}