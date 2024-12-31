package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(value = "课程基本信息管理接口", tags = "课程基本信息管理")
@RestController
public class CourseBaseInfoController {

    @Resource
    private CourseBaseInfoService courseBaseInfoService;

    @ApiOperation(value = "课程列表查询")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto) {

        return courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParamsDto);

    }

    @ApiOperation(value = "新增课程")
    @PostMapping("course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated AddCourseDto addCourseDto) {

        Long companyId = 1232141425L;

        return courseBaseInfoService.createCourseBase(companyId, addCourseDto);

    }

    @ApiOperation(value = "根据id查询课程")
    @GetMapping("course/{id}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long id) {

        return courseBaseInfoService.getCourseBaseInfo(id);

    }

    @ApiOperation(value = "修改课程")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated EditCourseDto editCourseDto) {

        Long companyId = 1232141425L;

        return courseBaseInfoService.updateCourseBase(companyId, editCourseDto);

    }

    @ApiOperation(value = "删除课程")
    @DeleteMapping("/course/{id}")
    public void deleteCourseBase(@PathVariable Long id) {

        courseBaseInfoService.deleteCourseBaseById(id);

    }


}
