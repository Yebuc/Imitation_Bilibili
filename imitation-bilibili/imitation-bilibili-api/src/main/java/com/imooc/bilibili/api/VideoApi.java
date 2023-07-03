package com.imooc.bilibili.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.api.support.UserSupport;
import com.imooc.bilibili.domain.*;
//import com.imooc.bilibili.service.ElasticSearchService;
import com.imooc.bilibili.service.VideoService;
//import org.apache.mahout.cf.taste.common.TasteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class VideoApi {

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserSupport userSupport;

//    @Autowired
//    private ElasticSearchService elasticSearchService;

    /**
     * 视频投稿
     */
    @PostMapping("/videos")
    public JsonResponse<String> addVideos(@RequestBody Video video){//这里应该还缺少一个上传文件的过程，后面在另一个方法里面再处理吧
        Long userId = userSupport.getCurrentUserId();//需要登录才能实现的功能
        video.setUserId(userId);
        videoService.addVideos(video);
        //在es中添加一条视频数据
//        elasticSearchService.addVideo(video);
        return JsonResponse.success();
    }

    /**
     * 分页查询视频列表
     */
    @GetMapping("/videos")
    public JsonResponse<PageResult<Video>> pageListVideos(Integer size, Integer no, String area){//每一页大小，页数，视频分区
        PageResult<Video> result = videoService.pageListVideos(size, no ,area);
        return new JsonResponse<>(result);
    }

    /**
     * 视频在线播放
     */
    @GetMapping("/video-slices")
    public void viewVideoOnlineBySlices(HttpServletRequest request,
                                        HttpServletResponse response,
                                        String url) {
        videoService.viewVideoOnlineBySlices(request, response, url);//因为我们是通过流的形式进行传输，所以这个流会写在http输出流里面，我们不需要具体的返回值
    }

    /**
     * 点赞视频
     */
    @PostMapping("/video-likes")
    public JsonResponse<String> addVideoLike(@RequestParam Long videoId){
        Long userId = userSupport.getCurrentUserId();
        videoService.addVideoLike(videoId, userId);
        return JsonResponse.success();
    }

    /**
     * 取消点赞视频
     */
    @DeleteMapping("/video-likes")
    public JsonResponse<String> deleteVideoLike(@RequestParam Long videoId){
        Long userId = userSupport.getCurrentUserId();
        videoService.deleteVideoLike(videoId, userId);
        return JsonResponse.success();
    }

    /**
     * 查询视频点赞数量   注意用户在未登录的模式下也是可以看视频的
     */
    @GetMapping("/video-likes")
    public JsonResponse<Map<String, Object>> getVideoLikes(@RequestParam Long videoId){
        Long userId = null;
        try{
            userId = userSupport.getCurrentUserId();
        }catch (Exception ignored){}//忽略异常  可以省去前端和后端对游客身份的判断
        Map<String, Object> result = videoService.getVideoLikes(videoId, userId);
        return new JsonResponse<>(result);
    }


    /**
     * 收藏视频
     */
    @PostMapping("/video-collections")
    public JsonResponse<String> addVideoCollection(@RequestBody VideoCollection videoCollection){
        Long userId = userSupport.getCurrentUserId();
        videoService.addVideoCollection(videoCollection, userId);
        return JsonResponse.success();
    }

    /**
     * 取消收藏视频
     */
    @DeleteMapping("/video-collections")
    public JsonResponse<String> deleteVideoCollection(@RequestParam Long videoId){
        Long userId = userSupport.getCurrentUserId();
        videoService.deleteVideoCollection(videoId, userId);
        return JsonResponse.success();
    }

    /**
     * 查询视频收藏数量
     */
    @GetMapping("/video-collections")
    public JsonResponse<Map<String, Object>> getVideoCollections(@RequestParam Long videoId){
        Long userId = null;
        try{
            userId = userSupport.getCurrentUserId();//和视频点赞一样，也要区分游客模式和普通用户模式的处理方式
        }catch (Exception ignored){}
        Map<String, Object> result = videoService.getVideoCollections(videoId, userId);
        return new JsonResponse<>(result);
    }

    /*
    差了几个和收藏分组相关的API，需要续写完整，后期补充
     */




    /**
     * 视频投币
     */
    @PostMapping("/video-coins")
    public JsonResponse<String> addVideoCoins(@RequestBody VideoCoin videoCoin){//游客投不了币
        Long userId = userSupport.getCurrentUserId();
        videoService.addVideoCoins(videoCoin, userId);
        return JsonResponse.success();
    }

    /**
     * 查询视频投币数量
     */
    @GetMapping("/video-coins")
    public JsonResponse<Map<String, Object>> getVideoCoins(@RequestParam Long videoId){
        Long userId = null;
        try{
            userId = userSupport.getCurrentUserId();
        }catch (Exception ignored){}
        Map<String, Object> result = videoService.getVideoCoins(videoId, userId);
        return new JsonResponse<>(result);
    }

    /**
     * 添加视频评论
     */
    @PostMapping("/video-comments")
    public JsonResponse<String> addVideoComment(@RequestBody VideoComment videoComment){
        Long userId = userSupport.getCurrentUserId();
        videoService.addVideoComment(videoComment, userId);
        return JsonResponse.success();
    }

    /**
     * 分页查询视频评论
     */
    @GetMapping("/video-comments")
    public JsonResponse<PageResult<VideoComment>> pageListVideoComments(@RequestParam Integer size,//@RequestParam表示必传参数
                                                                        @RequestParam Integer no,
                                                                        @RequestParam Long videoId){
        PageResult<VideoComment> result = videoService.pageListVideoComments(size, no, videoId);
        return new JsonResponse<>(result);
    }

    /**
     * 获取视频详情
     */
    @GetMapping("/video-details")
    public JsonResponse<Map<String, Object>> getVideoDetails(@RequestParam Long videoId){
        Map<String, Object> result = videoService.getVideoDetails(videoId);
        return new JsonResponse<>(result);
    }

    /**
     * 添加视频观看记录
     */
//    @PostMapping("/video-views")//VideoView中的clientID是什么意思？
//    public JsonResponse<String> addVideoView(@RequestBody VideoView videoView,
//                                             HttpServletRequest request){
//        Long userId;
//        try{
//            userId = userSupport.getCurrentUserId();
//            videoView.setUserId(userId);
//            videoService.addVideoView(videoView, request);
//        }catch (Exception e){
//            videoService.addVideoView(videoView, request);
//        }
//        return JsonResponse.success();
//    }

    /**
     * 查询视频播放量
     */
//    @GetMapping("/video-view-counts")
//    public JsonResponse<Integer> getVideoViewCounts(@RequestParam Long videoId){
//        Integer count = videoService.getVideoViewCounts(videoId);
//        return new JsonResponse<>(count);
//    }

    /**
     * 视频内容推荐
     */
//    @GetMapping("/recommendations")
//    public JsonResponse<List<Video>> recommend() throws TasteException {
//        Long userId = userSupport.getCurrentUserId();
//        List<Video> list = videoService.recommend(userId);
//        return new JsonResponse<>(list);
//    }

    /**
     * 视频帧截取生成黑白剪影
     */
//    @GetMapping("/video-frames")
//    public JsonResponse<List<VideoBinaryPicture>> captureVideoFrame(@RequestParam Long videoId,
//                                                                    @RequestParam String fileMd5) throws Exception {
//        List<VideoBinaryPicture> list = videoService.convertVideoToImage(videoId, fileMd5);
//        return new JsonResponse<>(list);
//    }
//
//    /**
//     * 查询视频黑白剪影
//     */
//    @GetMapping("/video-binary-images")
//    public JsonResponse<List<VideoBinaryPicture>> getVideoBinaryImages(@RequestParam Long videoId,
//                                                                       Long videoTimestamp,
//                                                                       String frameNo) {
//        Map<String, Object> params = new HashMap<>();
//        params.put("videoId", videoId);
//        params.put("videoTimestamp", videoTimestamp);
//        params.put("frameNo", frameNo);
//        List<VideoBinaryPicture> list = videoService.getVideoBinaryImages(params);
//        return new JsonResponse<>(list);
//    }
//
//    /**
//     * 查询视频标签
//     */
//    @GetMapping("/video-tags")
//    public JsonResponse<List<VideoTag>> getVideoTagsByVideoId(@RequestParam Long videoId) {
//        List<VideoTag> list = videoService.getVideoTagsByVideoId(videoId);
//        return new JsonResponse<>(list);
//    }

    /**
     * 删除视频标签
     */
//    @DeleteMapping("/video-tags")
//    public JsonResponse<String> deleteVideoTags(@RequestBody JSONObject params) {
//        String tagIdList = params.getString("tagIdList");
//        Long videoId = params.getLong("videoId");
//        videoService.deleteVideoTags(JSONArray.parseArray(tagIdList).toJavaList(Long.class), videoId);
//        return JsonResponse.success();
//    }
}
