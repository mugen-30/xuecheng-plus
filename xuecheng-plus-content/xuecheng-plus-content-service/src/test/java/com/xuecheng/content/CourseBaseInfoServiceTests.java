package com.xuecheng.content;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class CourseBaseInfoServiceTests {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;


    @Test
    void testCourseBaseInfoService() {

        //查询条件封装
        QueryCourseParamsDto queryCourseParamsDto = new QueryCourseParamsDto();
        queryCourseParamsDto.setCourseName("java");//课程名称
        queryCourseParamsDto.setAuditStatus("202004");//审核状态
        queryCourseParamsDto.setPublishStatus("203001");//发布状态

        //分页参数封装
        PageParams pageParams = new PageParams();
        pageParams.setPageNo(1L);//页码
        pageParams.setPageSize(3L);//每页记录数

        //执行查询
        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(null, pageParams, queryCourseParamsDto);

        System.out.println(courseBasePageResult);

    }

}