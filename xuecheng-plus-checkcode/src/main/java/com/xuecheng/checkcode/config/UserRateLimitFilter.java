package com.xuecheng.checkcode.config;

import com.xuecheng.base.exception.XueChengPlusException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserRateLimitFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException , XueChengPlusException {
        // 如果请求URI是"/checkcode/pic"
        if ("/checkcode/pic".equals(request.getRequestURI())) {
            // 获取客户端IP地址
            String clientIp = request.getRemoteAddr();

            // 从buckets映射中获取或创建对应的Bucket
            Bucket bucket = buckets.computeIfAbsent(clientIp, this::newBucket);
            // 尝试从Bucket中消费一个令牌
            if (bucket.tryConsume(1)) {
                // 如果消费成功，则继续执行过滤器链
                filterChain.doFilter(request, response);
            } else {
                // 如果消费失败，设置HTTP状态码为429（Too Many Requests）
//                response.setStatus(429);
                // 通过响应的Writer写入错误信息
//                response.getWriter().write("Too many requests - try again later");
                // 抛出运行时异常，提示用户点击太快，请稍后重试
                XueChengPlusException.cast("点击太快了，请稍后重试");
            }
        } else {
            // 如果请求URI不是"/checkcode/pic"，则继续执行过滤器链
            filterChain.doFilter(request, response);
        }
    }


    private Bucket newBucket(String clientIp) {
        Bandwidth limit = Bandwidth.classic(1, Refill.greedy(1, Duration.ofMillis(500)));
        return Bucket.builder().addLimit(limit).build();
    }
}