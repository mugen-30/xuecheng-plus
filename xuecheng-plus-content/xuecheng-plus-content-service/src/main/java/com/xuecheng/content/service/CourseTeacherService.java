package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

public interface CourseTeacherService {

    /**
     * 查询课程讲师列表
     * @param courseId 课程ID
     */
    public List<CourseTeacher> queryCourseTeacherList(Long courseId);

    /**
     * 保存课程讲师信息
     * @param companyId 机构ID
     * @param courseTeacher 课程讲师对象
     */
    public CourseTeacher saveCourseTeacher(Long companyId, CourseTeacher courseTeacher);

    /**
     * 删除课程讲师信息
     * @param courseId 课程ID
     * @param id 讲师ID
     */
    public void deleteCourseTeacher(Long courseId, String id);
}
