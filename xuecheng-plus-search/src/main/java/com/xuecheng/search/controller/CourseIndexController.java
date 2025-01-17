package com.xuecheng.search.controller;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.search.po.CourseIndex;
import com.xuecheng.search.service.IndexService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author mugen
 * @description 课程索引接口
 */
@Api(value = "课程信息索引接口", tags = "课程信息索引接口")
@RestController
@RequestMapping("/index")
public class CourseIndexController {

    @Resource
    IndexService indexService;
    @Value("${elasticsearch.course.index}")
    private String courseIndexStore;

    @ApiOperation("添加课程索引")
    @PostMapping("course")
    public Boolean add(@RequestBody CourseIndex courseIndex) {

        Long id = courseIndex.getId();
        if (id == null) {
            XueChengPlusException.cast("课程id为空");
            return false;
        }
        Boolean result = indexService.addCourseIndex(courseIndexStore, String.valueOf(id), courseIndex);
        if (!result) {
            XueChengPlusException.cast("添加课程索引失败");
            return false;
        }
        return true;

    }

    @ApiOperation("删除课程索引")
    @PostMapping("delete_course")
    public Boolean delete(@RequestBody CourseIndex courseIndex) {
        Long id = courseIndex.getId();
        if (id == null) {
            XueChengPlusException.cast("课程id为空");
            return false;
        }
        Boolean result = indexService.deleteCourseIndex(courseIndexStore, String.valueOf(id));
        if (!result) {
            XueChengPlusException.cast("删除课程索引失败");
            return false;
        }
        return true;
    }
}
