package com.xuecheng.content.api;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;

@Api(value = "课程发布接口", tags = "课程发布接口")
@Controller
@Slf4j
public class CoursePublishController {

    @Resource
    private CoursePublishService coursePublishService;

    @ApiOperation(value = "课程预览")
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId){

        ModelAndView modelAndView = new ModelAndView();
        try {
            // 查询数据
            CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreview(courseId);
            // 将查询结果添加到模型中
            modelAndView.addObject("model", coursePreviewInfo);
        } catch (Exception e) {
            modelAndView.addObject("model", new CoursePreviewDto());
            XueChengPlusException.cast("查询课程预览信息失败");
        }
        // 设置视图名称为course_template
        modelAndView.setViewName("course_template");
        // 返回ModelAndView对象
        return modelAndView; // 返回ModelAndView对象

    }

    @ApiOperation("提交审核")
    @ResponseBody
    @PostMapping ("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId){
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null){
            XueChengPlusException.cast("请登陆");
        }
        Long companyId = Long.valueOf(user.getCompanyId());
        coursePublishService.commitAudit(companyId, courseId);
    }

    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping("/coursepublish/{courseId}")
    public void coursePublish(@PathVariable("courseId") Long courseId){
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null){
            XueChengPlusException.cast("请登陆");
        }
        Long companyId = Long.valueOf(user.getCompanyId());
        coursePublishService.publish(companyId, courseId);
    }

    @ApiOperation("查询课程发布信息")
    @ResponseBody
    @GetMapping("/r/coursepublish/{courseId}")
    public CoursePublish getCoursePublishInfo(@PathVariable("courseId") Long courseId){
        CoursePublish byId = coursePublishService.getById(courseId);
        log.info("查询课程发布信息:{}", byId);
        return byId;
    }

    @ApiOperation("下架课程")
    @ResponseBody
    @GetMapping("/courseoffline/{courseId}")
    public void offline(@PathVariable("courseId") Long courseId){
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null){
            XueChengPlusException.cast("请登陆");
        }
        Long companyId = Long.valueOf(user.getCompanyId());
        coursePublishService.offline(companyId, courseId);
    }

    @ApiOperation("获取课程发布信息")
    @ResponseBody
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getCoursePublish(@PathVariable("courseId") Long courseId){

        return coursePublishService.getPublishCoursePreview(courseId);

    }



}
    