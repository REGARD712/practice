package com.coding24h.mybatiscrud.service;

import com.coding24h.mybatiscrud.entity.Dept;
import java.util.List;

public interface DeptService {
    Dept getById(Long id);
    List<Dept> getAll() ;
    void add(Dept dept);
    void update(Dept dept);
    void delete(Long id);
}
