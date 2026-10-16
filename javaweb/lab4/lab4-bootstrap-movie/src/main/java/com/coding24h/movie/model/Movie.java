package com.coding24h.movie.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Movie {
    private String title;
    private String director;
    private int releaseYear;
    private double rating;
    private String summary;
    private List<String> actors;
}
