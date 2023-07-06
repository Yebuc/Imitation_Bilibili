package com.imooc.bilibili.api;

import com.imooc.bilibili.domain.JsonResponse;
import com.imooc.bilibili.domain.Video;
import com.imooc.bilibili.service.DemoService;
import com.imooc.bilibili.service.ElasticSearchService;
import com.imooc.bilibili.service.util.FastDFSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author Amber
 * @create 2023-06-18 15:04
 */
@RestController
public class DemoApi {
    @Autowired
    private DemoService demoService;

    @Autowired
    private FastDFSUtil fastDFSUtil;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @GetMapping("/query")
    public Map<String,Object> query(Long id){
        return demoService.query(id);
    }

    @GetMapping("/slices")//文件分片接口
    public void slices(MultipartFile file) throws Exception {
        fastDFSUtil.convertFileToSlices(file);
    }

    @GetMapping("/es-videos")
    public JsonResponse<Video> getEsVideos(@RequestParam String keyword){
        Video video = elasticSearchService.getVideos(keyword);
        return new JsonResponse<>(video);
    }

//    @GetMapping("/demos")
//    public Long msget(@RequestParam Long id){
//        return msDeclareService.msget(id);
//    }

//    @PostMapping("/demos")
//    public Map<String, Object> mspost(@RequestBody Map<String, Object> params){
//        return msDeclareService.mspost(params);
//    }
//
//    @HystrixCommand(fallbackMethod = "error",
//            commandProperties = {
//                    @HystrixProperty(
//                            name = "execution.isolation.thread.timeoutInMilliseconds",
//                            value = "2000"
//                    )
//            }
//    )
//    @GetMapping("/timeout")
//    public String circuitBreakerWithHystrix(@RequestParam Long time){
//        return msDeclareService.timeout(time);
//    }

    public String error(Long time){
        return "超时出错！";
    }

}
