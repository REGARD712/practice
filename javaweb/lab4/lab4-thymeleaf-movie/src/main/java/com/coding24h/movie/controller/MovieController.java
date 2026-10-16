package com.coding24h.movie.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Controller
public class MovieController {
    @GetMapping("/")
    public String index() {
        return "redirect:/movie";
    }

    @GetMapping("/movie")
    public String movie(Model model) {
        model.addAttribute("title", "肖申克的救赎");
        model.addAttribute("director", "弗兰克·德拉邦特");
        model.addAttribute("releaseYear", 1994);
        model.addAttribute("rating", 9.7);
        model.addAttribute("summary", "一场关于希望与自由的经典故事，银行家安迪因冤案入狱，在肖申克监狱中凭借智慧和毅力重获新生。");
        List<String> actors = Arrays.asList("蒂姆·罗宾斯", "摩根·弗里曼", "鲍勃·冈顿");
        model.addAttribute("actors", actors);
        model.addAttribute("today", LocalDate.now());
        return "movie";
    }
}