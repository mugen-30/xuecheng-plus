package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Resource
    private CourseTeacherMapper courseTeacherMapper;

    @Resource
    private CourseBaseMapper courseBaseMapper;

    @Override
    public List<CourseTeacher> queryCourseTeacherList(Long courseId) {

        LambdaQueryWrapper<CourseTeacher> QueryWrapper = new LambdaQueryWrapper<>();
        QueryWrapper.eq(CourseTeacher::getCourseId, courseId);
        return courseTeacherMapper.selectList(QueryWrapper);

    }

    @Transactional
    @Override
    public CourseTeacher saveCourseTeacher(Long companyId, CourseTeacher courseTeacher) {

        Long courseId = courseTeacher.getCourseId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);


        if (!companyId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("本机构只能修改本机构的课程");
        }

        if(courseTeacher.getId() == null){
            //新增
            courseTeacherMapper.insert(courseTeacher);
            return courseTeacher;
        }else{
            //修改
            courseTeacherMapper.updateById(courseTeacher);
            return courseTeacher;
        }

    }

    @Transactional
    @Override
    public void deleteCourseTeacher(Long courseId, String id) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId,courseId).eq(CourseTeacher::getId,id);
        courseTeacherMapper.delete(queryWrapper);
    }
}
