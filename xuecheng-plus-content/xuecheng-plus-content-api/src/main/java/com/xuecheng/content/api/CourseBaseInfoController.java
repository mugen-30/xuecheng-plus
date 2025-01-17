package com.xuecheng.content.api;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(value = "课程基本信息管理接口", tags = "课程基本信息管理")
@RestController
public class CourseBaseInfoController {

    @Resource
    private CourseBaseInfoService courseBaseInfoService;


    @ApiOperation(value = "课程列表查询")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto) {

        //获取当前用户所属机构ID
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null){
            XueChengPlusException.cast("请登陆");
        }
        Long companyId = Long.valueOf(user.getCompanyId());

        return courseBaseInfoService.queryCourseBaseList(companyId, pageParams, queryCourseParamsDto);

    }

    @ApiOperation(value = "新增课程")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_add')")
    @PostMapping("course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated AddCourseDto addCourseDto) {

        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null){
            XueChengPlusException.cast("请登陆");
        }
        Long companyId = Long.valueOf(user.getCompanyId());

        return courseBaseInfoService.createCourseBase(companyId, addCourseDto);

    }

    @ApiOperation(value = "根据id查询课程")
    @PreAuthorize("hasAuthority('xc_teachmanager_course')")
    @GetMapping("course/{id}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long id) {

        return courseBaseInfoService.getCourseBaseInfo(id);

    }

    @ApiOperation(value = "修改课程")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_base')")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated EditCourseDto editCourseDto) {

        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null){
            XueChengPlusException.cast("请登陆");
        }
        Long companyId = Long.valueOf(user.getCompanyId());

        return courseBaseInfoService.updateCourseBase(companyId, editCourseDto);

    }

    @ApiOperation(value = "删除课程")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_del')")
    @DeleteMapping("/course/{id}")
    public void deleteCourseBase(@PathVariable Long id) {

        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null){
            XueChengPlusException.cast("请登陆");
        }
        Long companyId = Long.valueOf(user.getCompanyId());

        courseBaseInfoService.deleteCourseBaseById(companyId,id);

    }



}
