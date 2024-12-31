package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

public interface CourseBaseInfoService {

    /**
     * 课程分页查询
     *
     * @param pageParams           分页参数
     * @param queryCourseParamsDto 查询参数
     * @return 查询结果
     */
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /**
     * 创建课程基本信息
     *
     * @param companyId    所属机构ID
     * @param addCourseDto 新增课程信息
     * @return 返回新增的课程对象
     */
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);

    /**
     * 根据课程ID查询课程基本信息
     *
     * @param courseId 课程ID
     * @return 返回课程信息
     */
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    /**
     * 更新课程基本信息
     *
     * @param companyId     所属机构ID
     * @param editCourseDto 新增课程信息
     * @return 返回更新后的课程对象
     */
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto);

    /**
     * 删除课程基本信息
     * @param id 课程ID
     */
    public void deleteCourseBaseById(Long id);
}
