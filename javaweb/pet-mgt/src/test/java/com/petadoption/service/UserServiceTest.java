package com.petadoption.service;

import com.petadoption.dto.UserForm;
import com.petadoption.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void findByUsername() {
        User user = userService.findByUsername("zhangsan");
        System.out.println(user);
    }

    @Test
    void getById() {
        User user = userService.getById(10L);
        System.out.println(user);
    }

    @Test
    void findAll() {
        List<User> users = userService.findAll();
        users.forEach(System.out::println);
    }

    @Test
    void findAllRoles() {
        userService.findAllRoles().forEach(System.out::println);
    }

    @Test
    void count() {
        System.out.println(userService.count());
    }

    @Transactional
    @Test
    void newForm() {
        UserForm form = userService.newForm();
        System.out.println(form);
    }

}