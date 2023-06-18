package com.imooc.bilibili.service;

import com.imooc.bilibili.dao.MydbDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Amber
 * @create 2023-06-18 14:59
 */
@Service
public class DemoService {

    @Autowired
    private MydbDao mydbDao;

    public Map<String,Object> query(Long id){
        return mydbDao.query(id);
    }
}
