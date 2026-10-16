package com.coding24h.movie.controller;
import com.coding24h.movie.model.Movie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.time.LocalDate;
import java.util.Arrays;

@Controller
public class MovieController {
    @GetMapping("/")
    public String getMovies(Model model) {
        Movie movie1 = new Movie("肖申克的救赎", "弗兰克·德拉邦特", 1994, 9.7,
                "一场关于希望与自由的经典故事。", Arrays.asList("蒂姆·罗宾斯", "摩根·弗里曼"));
        Movie movie2 = new Movie("盗梦空间", "克里斯托弗·诺兰", 2010, 9.3,
                "梦境与现实交织的科幻巨制。", Arrays.asList("莱昂纳多·迪卡普里奥", "约瑟夫·高登-莱维特"));
        Movie movie3 = new Movie("寻梦环游记", "李·昂克里奇", 2017, 9.1,
                "关于家庭与梦想的感人动画。", Arrays.asList("安东尼·冈萨雷斯", "盖尔·加西亚·贝纳尔"));
        model.addAttribute("movies", Arrays.asList(movie1, movie2, movie3));
        model.addAttribute("today", LocalDate.now());
        return "movie";
    }
}