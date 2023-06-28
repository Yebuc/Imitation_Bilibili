package com.imooc.bilibili.api;

import com.imooc.bilibili.domain.JsonResponse;
import com.imooc.bilibili.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileApi {//文件上传的API

    @Autowired
    private FileService fileService;

    @PostMapping("/md5files")//MD5文件内容加密接口   也可以结合断点续传一起使用
    public JsonResponse<String> getFileMD5(MultipartFile file) throws Exception {//文件名要和前端传过来的文件名一致，要不然读取不到数据
        String fileMD5 = fileService.getFileMD5(file);
        return new JsonResponse<>(fileMD5);
    }

    @PutMapping("/file-slices")//文件分片上传接口----->可以实现断点续传
    public JsonResponse<String> uploadFileBySlices(MultipartFile slice,
                                                   String fileMd5,
                                                   Integer sliceNo,
                                                   Integer totalSliceNo) throws Exception {
        String filePath = fileService.uploadFileBySlices(slice, fileMd5, sliceNo, totalSliceNo);
        return new JsonResponse<>(filePath);
    }

}
