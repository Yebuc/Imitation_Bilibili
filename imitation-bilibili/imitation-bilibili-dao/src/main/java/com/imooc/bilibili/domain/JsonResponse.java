package com.imooc.bilibili.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Amber
 * @create 2023-06-20 15:11
 * Json返回格式设置
 */
@Data
public class JsonResponse<T> {

    private String code;//返回码

    private String msg;//提示信息

    private T data;//返回数据格式

    public JsonResponse(String code,String msg){
        this.code = code;
        this.msg = msg;
    }
    public JsonResponse(T data){
        this.data = data;
        this.code = "0";
        this.msg = "success";
    }

    public static JsonResponse<String> success(){//封装返回给前端的数据格式
        return new JsonResponse<>(null);
    }

    public static JsonResponse<String> success(String data){//封装返回给前端的数据格式
        return new JsonResponse<>(data);
    }

    public static JsonResponse<String> fail(){//封装返回给前端的数据格式
        return new JsonResponse<>("1","fail");
    }

    public static JsonResponse<String> fail(String code,String msg){//封装返回给前端的数据格式  特定的返回信息
        return new JsonResponse<>(code,msg);
    }

}
