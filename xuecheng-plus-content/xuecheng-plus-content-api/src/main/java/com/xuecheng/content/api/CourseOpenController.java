package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(value = "课程预览接口", tags = "课程预览接口")
@RestController
@RequestMapping("/open")
public class CourseOpenController {

    @Resource
    private CoursePublishService coursePublishService;

    @ApiOperation("获取课程详情")
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getCoursePreview(@PathVariable Long courseId) {
            return coursePublishService.getCoursePreview(courseId);
    }

}
