package com.imooc.bilibili.service.util;

import com.github.tobato.fastdfs.domain.fdfs.FileInfo;
import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadCallback;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.imooc.bilibili.domain.exception.ConditionException;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

@Component
public class FastDFSUtil {

    @Autowired
    private FastFileStorageClient fastFileStorageClient;//fastdfs提供给客户端想服务器进行交互的实体类

    @Autowired
    private AppendFileStorageClient appendFileStorageClient;//fastdfs内置工具类，可以专门针对断点续传这个功能

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String PATH_KEY = "path-key:";

    private static final String UPLOADED_SIZE_KEY = "uploaded-size-key:";

    private static final String UPLOADED_NO_KEY = "uploaded-no-key:";

    private static final String DEFAULT_GROUP = "group1";

    private static final int SLICE_SIZE = 1024 * 1024 * 2;//大小为2MB

//    @Value("${fdfs.http.storage-addr}")
    private String httpFdfsStorageAddr;

    public String getFileType(MultipartFile file){//获取文件类型方法
        if(file == null){
            throw new ConditionException("非法文件！");
        }
        String fileName = file.getOriginalFilename();//获取到文件名称
        int index = fileName.lastIndexOf(".");//切取分隔符,得到文件类型的分割点
        return fileName.substring(index+1);//得到文件类型并以字符串表示
    }

    //上传
    public String uploadCommonFile(MultipartFile file) throws Exception {//针对于一般的文件(大文件可能除外)
        Set<MetaData> metaDataSet = new HashSet<>();//MetaData可以为空
        String fileType = this.getFileType(file);
        StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), fileType, metaDataSet);
        return storePath.getPath();//返回上传文件的存储路径   这里的storePath.getPath()获取的是相对路径，没有前面的Group分组，因为确实也不需要，每台storage中所存储的东西都是一样的
    }

    public String uploadCommonFile(File file, String fileType) throws Exception {
        Set<MetaData> metaDataSet = new HashSet<>();
        StorePath storePath = fastFileStorageClient.uploadFile(new FileInputStream(file),
                                    file.length(), fileType, metaDataSet);
        return storePath.getPath();
    }

    //上传可以断点续传的文件
    public String uploadAppenderFile(MultipartFile file) throws Exception{
        String fileType = this.getFileType(file);
        StorePath storePath = appendFileStorageClient.uploadAppenderFile(DEFAULT_GROUP, file.getInputStream(), file.getSize(), fileType);
        return storePath.getPath();
    }

    public void modifyAppenderFile(MultipartFile file, String filePath, long offset) throws Exception{//分片上传，后面分片文件的添加与修改
        appendFileStorageClient.modifyFile(DEFAULT_GROUP, filePath, file.getInputStream(), file.getSize(), offset);
    }



    public String uploadFileBySlices(MultipartFile file, String fileMd5, Integer sliceNo, Integer totalSliceNo) throws Exception {//通过分片来进行文件分片上传  断点续传的开发
        if(file == null || sliceNo == null || totalSliceNo == null){
            throw new ConditionException("参数异常！");
        }
        String pathKey = PATH_KEY + fileMd5;//第一个分片传上去之后，系统会返回给用户对应文件的存储路径，以便后面继续分片上传----这个路径先暂时的放在Redis当中,等所有的文件分片都上传完毕之后，我们再清空这些消息
        String uploadedSizeKey = UPLOADED_SIZE_KEY + fileMd5;//当前已经上传的所有分片的总大小  在使用modifyAppenderFile中，参数offset偏移量就是根据uploadedSizeKey的值来得到的
        String uploadedNoKey = UPLOADED_NO_KEY + fileMd5;//当前一共上传了多少个分片了
        String uploadedSizeStr = redisTemplate.opsForValue().get(uploadedSizeKey);
        Long uploadedSize = 0L;//如果是第一个文件，则已上传文件大小就为0L
        if(!StringUtil.isNullOrEmpty(uploadedSizeStr)){
            uploadedSize = Long.valueOf(uploadedSizeStr);//已经上传文件的总大小
        }
        String fileType = this.getFileType(file);

        if(sliceNo == 1){ //上传的是第一个分片
            String path = this.uploadAppenderFile(file);
            if(StringUtil.isNullOrEmpty(path)){
                throw new ConditionException("上传失败！");
            }
            redisTemplate.opsForValue().set(pathKey, path);
            redisTemplate.opsForValue().set(uploadedNoKey, "1");
        }else{
            String filePath = redisTemplate.opsForValue().get(pathKey);
            if(StringUtil.isNullOrEmpty(filePath)){
                throw new ConditionException("上传失败！");
            }
            this.modifyAppenderFile(file, filePath, uploadedSize);//modifyAppenderFile这里可以控制断点续传
            redisTemplate.opsForValue().increment(uploadedNoKey);
        }
        // 修改历史上传分片文件大小
        uploadedSize  += file.getSize();
        redisTemplate.opsForValue().set(uploadedSizeKey, String.valueOf(uploadedSize));
        //如果所有分片全部上传完毕，则清空redis里面相关的key和value
        String uploadedNoStr = redisTemplate.opsForValue().get(uploadedNoKey);
        Integer uploadedNo = Integer.valueOf(uploadedNoStr);
        String resultPath = "";
        if(uploadedNo.equals(totalSliceNo)){
            resultPath = redisTemplate.opsForValue().get(pathKey);
            List<String> keyList = Arrays.asList(uploadedNoKey, pathKey, uploadedSizeKey);
            redisTemplate.delete(keyList);
        }
        return resultPath;//返回系统存储路劲
    }


    //文件分片方法
    public void convertFileToSlices(MultipartFile multipartFile) throws Exception{
        String fileName = multipartFile.getOriginalFilename();
        String fileType = this.getFileType(multipartFile);
        //生成临时文件，将MultipartFile转为File
        File file = this.multipartFileToFile(multipartFile);
        long fileLength = file.length();//文件的大小
        int count = 1;//计数器
        for(int i = 0; i < fileLength; i += SLICE_SIZE){
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");//RandomAccessFile对文件的一个处理工具，可以进行读写文件--->特点是支持随机访问的方式
            randomAccessFile.seek(i);//seek--->搜寻到想要读取的开始位置
            byte[] bytes = new byte[SLICE_SIZE];
            int len = randomAccessFile.read(bytes);//返回是的实际读出来的数据长度--->因为最后一次读取数据的时候，最后一段的大小不一定为SLICE_SIZE
//            String path = "/Users/hat/tmpfile/" + count + "." + fileType;
            String path = "E:\\tmpfile\\" + count + "." + fileType;//自己在硬盘上建一个临时存储文件   这里为什么只能识别到E盘呢？没有加斜杠啊，注意反斜杠的转义意义
            File slice = new File(path);//每个分片文件
            FileOutputStream fos = new FileOutputStream(slice);
            fos.write(bytes, 0, len);
            fos.close();
            randomAccessFile.close();
            count++;
        }
        //删除临时文件
        file.delete();
    }

    public File multipartFileToFile(MultipartFile multipartFile) throws Exception{//将MultipartFile类型转换为IO包中的file类型
        String originalFileName = multipartFile.getOriginalFilename();//originalFileName带着名称和类型的
        String[] fileName = originalFileName.split("\\.");//得到名称，不要类型
        File file = File.createTempFile("temp_"+fileName[0], "." + fileName[1]);//生成临时文件
        multipartFile.transferTo(file);//将multipartFile文件内容下入java类型中的file文件中
        return file;
    }


    //删除
    public void deleteFile(String filePath){
        fastFileStorageClient.deleteFile(filePath);
    }



    public void viewVideoOnlineBySlices(HttpServletRequest request,
                                        HttpServletResponse response,
                                        String path) throws Exception{
        FileInfo fileInfo = fastFileStorageClient.queryFileInfo(DEFAULT_GROUP, path);
        long totalFileSize = fileInfo.getFileSize();
        String url = httpFdfsStorageAddr + path;
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, Object> headers = new HashMap<>();
        while(headerNames.hasMoreElements()){
            String header = headerNames.nextElement();
            headers.put(header, request.getHeader(header));
        }
        String rangeStr = request.getHeader("Range");
        String[] range;
        if(StringUtil.isNullOrEmpty(rangeStr)){
            rangeStr = "bytes=0-" + (totalFileSize-1);
        }
        range = rangeStr.split("bytes=|-");
        long begin = 0;
        if(range.length >= 2){
            begin = Long.parseLong(range[1]);
        }
        long end = totalFileSize-1;
        if(range.length >= 3){
            end = Long.parseLong(range[2]);
        }
        long len = (end - begin) + 1;
        String contentRange = "bytes " + begin + "-" + end + "/" + totalFileSize;
        response.setHeader("Content-Range", contentRange);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Type", "video/mp4");
        response.setContentLength((int)len);
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        HttpUtil.get(url, headers, response);
    }

    //下载
    public void downLoadFile(String url, String localPath) {
        fastFileStorageClient.downloadFile(DEFAULT_GROUP, url,
                new DownloadCallback<String>() {
                    @Override
                    public String recv(InputStream ins) throws IOException {
                        File file = new File(localPath);
                        OutputStream os = new FileOutputStream(file);
                        int len = 0;
                        byte[] buffer = new byte[1024];
                        while ((len = ins.read(buffer)) != -1) {
                            os.write(buffer, 0, len);
                        }
                        os.close();
                        ins.close();
                        return "success";
                    }
                });
    }
}
