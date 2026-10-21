package com.coding24h.mybatiscrud.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Dept {
    private Long id;
    private String name;
    private String location;
    private Date createTime;
}
