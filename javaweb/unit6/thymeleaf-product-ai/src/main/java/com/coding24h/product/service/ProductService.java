package com.coding24h.product.service;

import com.coding24h.product.model.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    public List<Product> findAll() {
        return List.of(
                new Product(1L, "无线蓝牙耳机", new BigDecimal("199.00"), 25, "轻巧舒适，续航持久"),
                new Product(2L, "智能手表", new BigDecimal("599.00"), 8, "支持心率监测与运动记录"),
                new Product(3L, "机械键盘", new BigDecimal("329.00"), 15, "手感出色，适合办公与游戏"),
                new Product(4L, "便携式充电宝", new BigDecimal("129.00"), 5, "大容量设计，出行安心"),
                new Product(5L, "高清网络摄像头", new BigDecimal("259.00"), 12, "清晰画质，适用于视频会议")
        );
    }
}
