package com.imooc.bilibili.api;

import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Amber
 * @create 2023-06-18 16:17
 */
@RestController
public class RestfulApi {

    private final Map<Integer,Map<String,Object>> dataMap;

    public RestfulApi(){
        dataMap = new HashMap<>();//初始化dataMap
        for (int i = 1; i < 4; i++) {
            Map<String,Object> data = new HashMap<>();
            data.put("id",i);
            data.put("name","name"+i);
            dataMap.put(i,data);
        }

    }

    @GetMapping("/objects/{id}")//@PathVariable将参数id与括号内的id进行关联
    public Map<String,Object> getData(@PathVariable Integer id){
        return dataMap.get(id);
    }
    @DeleteMapping("/objects/{id}")
    public String deleteData(@PathVariable Integer id){
        dataMap.remove(id);
        return "delete success";
    }
    @PostMapping("/objects")
    public String postData(@RequestBody Map<String,Object> data){//缺少错误处理
        Integer[] idArray = dataMap.keySet().toArray(new Integer[0]);
        Arrays.sort(idArray);
        int nextId = idArray[idArray.length - 1] + 1;
        dataMap.put(nextId,data);
        return "put success";
    }

    @PutMapping("/objects")
    public String putData(@RequestBody Map<String,Object> data){
        Integer id = Integer.valueOf(String.valueOf(data.get("id")));
        Map<String, Object> containData = dataMap.get(id);
        if(containData == null){
            Integer[] idArray = dataMap.keySet().toArray(new Integer[0]);
            Arrays.sort(idArray);
            int nextId = idArray[idArray.length - 1] + 1;
            dataMap.put(nextId,data);
        }else{//更新
            dataMap.put(id,data);
        }
        return "put success";
    }

}
