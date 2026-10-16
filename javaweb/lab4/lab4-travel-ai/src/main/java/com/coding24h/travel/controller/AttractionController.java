package com.coding24h.travel.controller;

import com.coding24h.travel.model.Attraction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AttractionController {

    @GetMapping("/attractions")
    public String attractions(Model model) {
        List<Attraction> attractions = List.of(
                new Attraction(1L, "白马寺", "河南省洛阳市", "中国第一古刹，寺内古建筑与佛教文化交相辉映。", 5.0,
                        "/assets/白马寺.jpg"),
                new Attraction(2L, "洛邑古城", "河南省洛阳市", "漫步古城街巷，感受隋唐风韵与夜色中的洛阳烟火。", 4.5,
                        "/assets/洛邑古城.jpg"),
                new Attraction(3L, "天堂明堂", "河南省洛阳市", "武则天时期的皇家建筑群，再现盛唐宫廷风貌。", 5.0,
                        "/assets/天堂.jpg"),
                new Attraction(4L, "九州池瑶光殿", "河南省洛阳市", "园林水景与恢宏宫殿相映，是体验隋唐宫苑文化的好去处。", 4.0,
                        "/assets/九州池瑶光殿和天堂.jpg"),
                new Attraction(5L, "九寨沟五花海", "四川省阿坝州", "湖水色彩斑斓，周围森林倒映其中，景色如诗如画。", 5.0,
                        "/assets/四川九寨沟五花海.jpg"),
                new Attraction(6L, "九寨沟诺日朗瀑布", "四川省阿坝州", "宽阔壮观的钙华瀑布，四季皆有不同的自然风光。", 4.0,
                        "/assets/四川九寨沟诺日朗瀑布.jpg")
        );
        model.addAttribute("attractions", attractions);
        return "attractions";
    }
}
