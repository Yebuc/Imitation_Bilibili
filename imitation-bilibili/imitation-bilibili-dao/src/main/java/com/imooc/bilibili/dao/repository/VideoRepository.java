package com.imooc.bilibili.dao.repository;

import com.imooc.bilibili.domain.Video;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface VideoRepository extends ElasticsearchRepository<Video, Long> {//泛型---><repository所面向的类型，当前类所对应表中的主键id的类型>

    //spring data elasticSearch给我们提供了一套基于特殊命名的一套查询规则或者逻辑  如：当使用findByTitleLike时候，会将其进行关键词的拆解--find by title like
    Video findByTitleLike(String keyword);//模糊查询   es会自己识别哦
}
