package com.imooc.bilibili.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * @author Amber
 * @create 2023-06-18 14:40
 * 做项目启动的时候由于E:\APPDownload\Java\xiangmu\Imitation B station\Project\imitation-bilibili\imitation-bilibili-dao\src\main\resources\mapper\my_db2.xml
 * 中有指定关联，mybatis会自动将dao文件封装成一个实体类，会自动进行实例化的操作
 */
@Mapper
public interface MydbDao {

    Map<String,Object> query(Long id);
}
