package com.imooc.bilibili.api;

import com.imooc.bilibili.service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Amber
 * @create 2023-06-18 15:04
 */
@RestController
public class DemoApi {
    @Autowired
    private DemoService demoService;

    @GetMapping("/query")
    public Map<String,Object> query(Long id){
        return demoService.query(id);
    }
}
