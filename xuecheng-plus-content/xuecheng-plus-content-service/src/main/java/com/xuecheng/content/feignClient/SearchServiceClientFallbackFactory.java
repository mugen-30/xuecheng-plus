package com.xuecheng.content.feignClient;

import com.xuecheng.base.exception.XueChengPlusException;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SearchServiceClientFallbackFactory implements FallbackFactory<SearchServiceClient> {

    @Override
    public SearchServiceClient create(Throwable throwable) {
        return new SearchServiceClient() {
            @Override
            public Boolean add(CourseIndex courseIndex) {
                log.error("添加索引发生熔断:索引信息：{}，熔断信息：{}", courseIndex, throwable.toString());
                XueChengPlusException.cast("请稍后重试");
                return false;
            }
        };
    }
}
