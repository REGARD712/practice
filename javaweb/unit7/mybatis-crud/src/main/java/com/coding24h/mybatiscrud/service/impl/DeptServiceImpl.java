package com.coding24h.mybatiscrud.service.impl;

import com.coding24h.mybatiscrud.entity.Dept;
import com.coding24h.mybatiscrud.mapper.DeptMapper;
import com.coding24h.mybatiscrud.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DeptServiceImpl implements DeptService {
    @Autowired
    private DeptMapper deptMapper;

    @Override
    public Dept getById(Long id) {
        return deptMapper.selectById(id);
    }

    @Override
    public List<Dept> getAll() {
        return deptMapper.selectAll();
    }

    @Override
    public void add(Dept dept) {
        deptMapper.insert(dept);
    }

    @Override
    public void update(Dept dept) {
        deptMapper.update(dept);
    }

    @Override
    public void delete(Long id) {
        deptMapper.deleteById(id);
    }
}
