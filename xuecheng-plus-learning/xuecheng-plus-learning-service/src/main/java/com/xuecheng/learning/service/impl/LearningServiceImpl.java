package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.feignclient.MediaServiceClient;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class LearningServiceImpl implements LearningService {

    @Resource
    private MyCourseTablesService myCourseTablesService;

    @Resource
    private ContentServiceClient contentServiceClient;

    @Resource
    private MediaServiceClient mediaServiceClient;

    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {

        CoursePublish coursePublish = contentServiceClient.getCoursepublishInfo(courseId);
        if (coursePublish == null) {
            return RestResponse.validfail("课程不存在");
        }

        //判断是否收费
        String charge = coursePublish.getCharge();
        if ("201000".equals(charge)) {
            //免费，获取视频地址
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }

        //根据课程计划id的is_priview判断是否支持试学
        //解析出corsePublish的teachplan
        String teachplanString = coursePublish.getTeachplan();
        List<Teachplan> teachplans = JSON.parseArray(teachplanString, Teachplan.class);
        if (teachplans == null) {
            return RestResponse.validfail("课程计划不存在");
        }
        //根据teachplanId获取teachplan
        Teachplan teachplan = teachplans.stream().filter(t -> t.getId().equals(teachplanId)).findFirst().orElse(null);
        if (teachplan != null) {
            if ("1".equals(teachplan.getIsPreview())) {
                //支持试学，获取视频地址
                return mediaServiceClient.getPlayUrlByMediaId(mediaId);
            }
        }

        //判断学习资格
        if (userId != null) {
            XcCourseTablesDto xcCourseTablesDto = myCourseTablesService.getLearningStatus(userId, courseId);
            if("702002".equals(xcCourseTablesDto.getLearnStatus())){
                return RestResponse.validfail("无学习资格,请先进行支付");
            } else if ("702003".equals(xcCourseTablesDto.getLearnStatus())){
                return RestResponse.validfail("学习资格已过期");
            } else if ("702001".equals(xcCourseTablesDto.getLearnStatus())){
                //获取视频地址
                return mediaServiceClient.getPlayUrlByMediaId(mediaId);
            }
        }

        return RestResponse.validfail("获取视频失败");
    }

}
