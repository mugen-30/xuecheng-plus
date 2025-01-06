package com.xuecheng.content;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignClient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;


@SpringBootTest
public class FeignUploadTest {

    @Resource
    MediaServiceClient mediaServiceClient;

    //远程调用，上传文件
    @Test
    public void test() throws Exception {
    
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("D:\\develop\\test.html"));
        mediaServiceClient.upload(multipartFile,"course/test.html");
    }

}