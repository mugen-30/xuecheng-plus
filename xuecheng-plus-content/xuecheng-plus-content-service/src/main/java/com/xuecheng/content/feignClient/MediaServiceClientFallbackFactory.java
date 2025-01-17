package com.xuecheng.content.feignClient;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.RestResponse;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Slf4j
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {
    @Override
    public MediaServiceClient create(Throwable throwable) {
        return new MediaServiceClient() {
            @Override
            public String upload(MultipartFile filedata, String objectName) {
                log.error("远程调用上传文件接口发生熔断:{},{}", throwable.toString(), throwable.getMessage());
                XueChengPlusException.cast("请稍后重试");
                return null;
            }

            @Override
            public RestResponse<Boolean> delete(String id) {
                log.error("远程调用删除文件接口发生熔断:{},{}", throwable.toString(), throwable.getMessage());
                XueChengPlusException.cast("请稍后重试");
                return null;
            }
        };
    }
}
