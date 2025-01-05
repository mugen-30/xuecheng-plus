package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class CoursePublishServiceImpl extends ServiceImpl<CoursePublishMapper, CoursePublish> implements CoursePublishService {
    @Resource
    private CourseBaseInfoService courseBaseInfoService;

    @Resource
    private TeachPlanService teachPlanService;

    @Resource
    private CourseTeacherService courseTeacherService;

    @Resource
    private CourseMarketService courseMarketService;

    @Resource
    private CoursePublishPreService coursePublishPreService;

    @Resource
    private CoursePublishMapper coursePublishMapper;

    @Override
    public CoursePreviewDto getCoursePreview(Long courseId) {
        // 创建一个CoursePreviewDto对象
        CoursePreviewDto coursePreview = new CoursePreviewDto();

        // 获取课程基本信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        // 将课程基本信息设置到coursePreview对象中
        coursePreview.setCourseBase(courseBaseInfo);

        // 获取教学计划列表
        List<TeachPlanDto> teachplans = teachPlanService.queryTeachPlanTree(courseId);
        // 将教学计划列表设置到coursePreview对象中
        coursePreview.setTeachplans(teachplans);

        // 获取课程教师列表
        List<CourseTeacher> courseTeachers = courseTeacherService.queryCourseTeacherList(courseId);
        // 将课程教师列表设置到coursePreview对象中
        coursePreview.setCourseTeachers(courseTeachers);

        // 返回coursePreview对象
        return coursePreview;
    }

    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {
        CoursePreviewDto coursePreview = getCoursePreview(courseId);
        CourseBaseInfoDto courseBaseInfo = coursePreview.getCourseBase();
        List<TeachPlanDto> teachplans = coursePreview.getTeachplans();
        List<CourseTeacher> courseTeachers = coursePreview.getCourseTeachers();
        if (courseBaseInfo == null) {
            XueChengPlusException.cast("课程不存在");
        }
        if (!Objects.equals(companyId, courseBaseInfo.getCompanyId())){
            XueChengPlusException.cast("只能提交本机构的课程");
        }
        //审核状态
        String status = courseBaseInfo.getStatus();
        //如果课程状态为已提交，则不允许重复提交
        if(status.equals("202003")){
            XueChengPlusException.cast("课程已提交，不允许重复提交");
        }

        //课程的图片，课程信息是否完整
        String pic = courseBaseInfo.getPic();
        if (StringUtils.isEmpty(pic)){
            XueChengPlusException.cast("课程图片不能为空");
        }
        if (teachplans == null || teachplans.isEmpty()){
            XueChengPlusException.cast("课程计划不能为空");
        }
        if (courseTeachers == null || courseTeachers.isEmpty()){
            XueChengPlusException.cast("课程讲师不能为空");
        }

        //查询到课程基本信息，营销信息，计划，讲师等信息插入到课程预发布表
        // 创建CoursePublishPre对象
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        // 将courseBaseInfo的属性复制到coursePublishPre中
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        // 根据courseId获取对应的CourseMarket对象
        CourseMarket courseMarket = courseMarketService.getById(courseId);
        // 将courseMarket对象转换为JSON字符串
        String courseMarketJSON = JSON.toJSONString(courseMarket);
        // 将teachplans对象转换为JSON字符串
        String teachplansJSON = JSON.toJSONString(teachplans);
        // 将courseTeachers对象转换为JSON字符串
        String courseTeacherJSON = JSON.toJSONString(courseTeachers);
        // 设置coursePublishPre的market属性为courseMarketJSON
        coursePublishPre.setMarket(courseMarketJSON);
        // 设置coursePublishPre的teachplan属性为teachplansJSON
        coursePublishPre.setTeachplan(teachplansJSON);
        // 设置coursePublishPre的teachers属性为courseTeacherJSON
        coursePublishPre.setTeachers(courseTeacherJSON);
        // 设置coursePublishPre的审核状态为已提交
        coursePublishPre.setStatus("202003");
        // 设置coursePublishPre的提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        // 保存课程预发布表
        CoursePublishPre coursePublishPre1 = coursePublishPreService.getById(courseId);
        if (coursePublishPre1 == null){
            coursePublishPreService.save(coursePublishPre);
        } else {
            coursePublishPreService.updateById(coursePublishPre);
        }

        //更新课程基本信息表的审核状态为已提交
        CourseBase courseBase = courseBaseInfoService.getById(courseId);
        courseBase.setAuditStatus("202003");
        courseBaseInfoService.updateById(courseBase);

    }

    @Override
    public void publish(Long companyId, Long courseId) {

        CoursePublishPre coursePublishPre = coursePublishPreService.getById(courseId);
        if (coursePublishPre == null){
            XueChengPlusException.cast("课程没有审核记录，无法发布");
        }
        //审核状态
        String status = coursePublishPre.getStatus();
        if (!status.equals("202004")){
            XueChengPlusException.cast("课程审核未通过，无法发布");
        }
        //向课程发布表写入数据
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        //先查询发布表，有则更新，没有则插入
        CoursePublish coursePublishObj = coursePublishMapper.selectById(courseId);
        if (coursePublishObj == null){
            coursePublishMapper.insert(coursePublish);
        }else {
            coursePublishMapper.updateById(coursePublish);
        }

        //TODO:向消息表写入数据


        //将课程预发布表数据删除
        coursePublishPreService.removeById(courseId);

    }

}
