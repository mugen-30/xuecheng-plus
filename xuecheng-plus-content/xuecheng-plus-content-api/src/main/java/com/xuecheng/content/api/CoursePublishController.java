package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;

@Controller 
public class CoursePublishController {

    @Resource
    private CoursePublishService coursePublishService;

    @ApiOperation(value = "课程预览")
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId){

        // 查询数据
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreview(courseId); // 查询课程预览信息

        ModelAndView modelAndView = new ModelAndView();
        // 将查询结果添加到模型中
        modelAndView.addObject("model", coursePreviewInfo); // 将查询结果添加到模型中
        // 设置视图名称为course_template
        modelAndView.setViewName("course_template"); // 设置视图名称为course_template
        // 返回ModelAndView对象
        return modelAndView; // 返回ModelAndView对象

    }

    @ApiOperation("提交审核")
    @ResponseBody
    @PostMapping ("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId){
        Long companyId = 1232141425L;
        coursePublishService.commitAudit(companyId, courseId);
    }

    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping("/coursepublish/{courseId}")
    public void coursePublish(@PathVariable("courseId") Long courseId){
        Long companyId = 1232141425L;
        coursePublishService.publish(companyId, courseId);
    }


}
    