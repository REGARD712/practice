package com.coding24h.card.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class User {
    private String name;
    private int age;
    private String email;
    private String bio;
    private List<String> skills;
}
