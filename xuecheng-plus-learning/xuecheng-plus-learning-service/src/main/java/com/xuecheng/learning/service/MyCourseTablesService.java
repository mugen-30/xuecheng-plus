package com.xuecheng.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcCourseTables;

/**
 * <p>
 *  服务类
 * </p>
 */
public interface MyCourseTablesService extends IService<XcCourseTables> {

    /**
     * 添加选课
     * @param userId 用户id
     * @param courseId 课程id
     * @return XcChooseCourseDto 选课信息
     */
    XcChooseCourseDto addChooseCourse(String userId, Long courseId);

    /**
     * 查询学习资格
     * @param userId 用户id
     * @param courseId 课程id
     */
    XcCourseTablesDto getLearningStatus(String userId, Long courseId);

    /**
     * 保存选课成功
     * @param chooseCourseId 选课id
     * @return boolean
     */
    boolean saveChooseCourseSuccess(String chooseCourseId);

    /**
     * @description 我的课程表
     * @param params
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.learning.model.po.XcCourseTables>
     */
    public PageResult<XcCourseTables> mycoursetables(MyCourseTableParams params);
}
