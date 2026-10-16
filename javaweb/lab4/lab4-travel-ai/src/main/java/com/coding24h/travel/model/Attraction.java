package com.coding24h.travel.model;

import lombok.Data;

@Data
public class Attraction {

    private Long id;
    private String name;
    private String location;
    private String description;
    private Double rating;
    private String imageUrl;

    public Attraction() {
    }

    public Attraction(Long id, String name, String location, String description, Double rating, String imageUrl) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.description = description;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }
}
