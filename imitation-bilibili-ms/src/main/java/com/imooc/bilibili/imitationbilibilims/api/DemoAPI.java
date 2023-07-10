package com.imooc.bilibili.imitationbilibilims.api;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Amber
 * @create 2023-07-07 20:19
 */
@RestController
public class DemoAPI {

    @GetMapping("/demos")
    public Long msg(@RequestParam Long id){
        return id;
    }

    @PostMapping("/demos")
    public Map<String,Object> mspost(@RequestBody Map<String,Object> params){
        return params;
    }

    @GetMapping("/timeout")
    public String timeout(@RequestParam Long time){
        try {
            Thread.sleep(time);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return "熔断测试！！！";
    }
}
