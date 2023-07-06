package com.imooc.bilibili.service;

import com.imooc.bilibili.dao.repository.UserInfoRepository;
import com.imooc.bilibili.dao.repository.VideoRepository;
import com.imooc.bilibili.domain.UserInfo;
import com.imooc.bilibili.domain.Video;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ElasticSearchService {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public void addUserInfo(UserInfo userInfo){
        userInfoRepository.save(userInfo);
    }

    public void addVideo(Video video){
        videoRepository.save(video);
    }

    //全文搜索功能   在SystemAPI里面测试接口方法
    public List<Map<String, Object>> getContents(String keyword,
                                                 Integer pageNo,
                                                 Integer pageSize) throws IOException {
        String[] indices = {"videos", "user-infos"};//ES当中的索引名称
        SearchRequest searchRequest = new SearchRequest(indices);//ES中查询请求的构建
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();//ES提供的一个原生的功能
        //分页
        sourceBuilder.from(pageNo - 1);
        sourceBuilder.size(pageSize);
        MultiMatchQueryBuilder matchQueryBuilder = QueryBuilders.multiMatchQuery(keyword, "title", "nick", "description");//多条件下查询的构建器
        sourceBuilder.query(matchQueryBuilder);//存储配置  和MP使用相类似
        searchRequest.source(sourceBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));//超时
        //高亮显示
        String[] array = {"title", "nick", "description"};
        HighlightBuilder highlightBuilder = new HighlightBuilder();//高亮实体类HighlightBuilder
        for(String key : array){
            highlightBuilder.fields().add(new HighlightBuilder.Field(key));
        }
        highlightBuilder.requireFieldMatch(false); //如果要多个字段进行高亮，要为false
        highlightBuilder.preTags("<span style=\"color:red\">");//在返回字段中需要高亮的内容新添加上标签返回
        highlightBuilder.postTags("</span>");//在返回字段中需要高亮的内容新添加上标签返回
        sourceBuilder.highlighter(highlightBuilder);//设置
        //执行搜索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        List<Map<String, Object>> arrayList = new ArrayList<>();
        for(SearchHit hit : searchResponse.getHits()){//searchResponse.getHits()表示全文查询中击中的条目
            //处理高亮字段
            Map<String, HighlightField> highLightBuilderFields = hit.getHighlightFields();//获取高亮的区域
            Map<String, Object> sourceMap = hit.getSourceAsMap();//用来存放处理完的内容
            for(String key : array){
                HighlightField field = highLightBuilderFields.get(key);
                if(field != null){
                    Text[] fragments = field.fragments();//获取到的内容有可能是一个数组，多条内容
                    String str = Arrays.toString(fragments);
                    str = str.substring(1, str.length()-1);//转换的是----->列表转化字符串，它是有头和尾的，需要去除中括号“[]”
                    sourceMap.put(key, str);
                }
            }
            arrayList.add(sourceMap);
        }
        return arrayList;
    }


    public Video getVideos(String keyword){
       return videoRepository.findByTitleLike(keyword);//title关键词模糊查询
    }

    public void deleteAllVideos(){
        videoRepository.deleteAll();
    }
}
