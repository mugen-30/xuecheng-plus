package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Resource
    CourseBaseMapper courseBaseMapper;

    @Resource
    CourseMarketMapper courseMarketMapper;

    @Resource
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {

        //查询条件封装对象
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        //拼接查询条件
        //根据课程名称模糊查询  name like '%名称%'
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),CourseBase::getName,queryCourseParamsDto.getCourseName());
        //根据课程审核状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus());
        //根据课程发布状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),CourseBase::getStatus,queryCourseParamsDto.getPublishStatus());

        //分页参数封装对象
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        //分页查询E page 分页参数, @Param("ew") Wrapper<T> queryWrapper 查询条件
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);

        //数据
        List<CourseBase> items = pageResult.getRecords();
        //总记录数
        long total = pageResult.getTotal();

        //封装返回结果
        return new PageResult<>(items, total, pageParams.getPageNo(), pageParams.getPageSize());

    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {

//        //参数的合法性校验
//        if (StringUtils.isBlank(dto.getName())) {
//            XueChengPlusException.cast("课程名称为空");
//        }
//
//        if (StringUtils.isBlank(dto.getMt())) {
//            XueChengPlusException.cast("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(dto.getSt())) {
//            XueChengPlusException.cast("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(dto.getGrade())) {
//            XueChengPlusException.cast("课程等级为空");
//        }
//
//        if (StringUtils.isBlank(dto.getTeachmode())) {
//            XueChengPlusException.cast("教育模式为空");
//        }
//
//        if (StringUtils.isBlank(dto.getUsers())) {
//            XueChengPlusException.cast("适应人群为空");
//        }
//
//        if (StringUtils.isBlank(dto.getCharge())) {
//            XueChengPlusException.cast("收费规则为空");
//        }

        //向课程基本信息表写入数据
        //新增对象
        CourseBase courseBaseNew = new CourseBase();
        //将填写的课程信息赋值给新增对象
        BeanUtils.copyProperties(dto,courseBaseNew);
        //设置审核状态
        courseBaseNew.setAuditStatus("202002");
        //设置发布状态
        courseBaseNew.setStatus("203001");
        //机构id
        courseBaseNew.setCompanyId(companyId);
        //添加时间
        courseBaseNew.setCreateDate(LocalDateTime.now());
        //插入课程基本信息表
        int insert = courseBaseMapper.insert(courseBaseNew);
        if(insert<=0){
            throw new XueChengPlusException("新增课程基本信息失败");
        }

        //向课程营销表写入数据
        CourseMarket courseMarket = new CourseMarket();
        //将填写的课程信息赋值给营销信息对象
        BeanUtils.copyProperties(dto,courseMarket);
        //设置课程id
        courseMarket.setId(courseBaseNew.getId());
        //保存课程营销信息
        saveCourseMarket(courseMarket);

        return getCourseBaseInfo(courseBaseNew.getId());

    }

    @Override
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {
        // 根据课程ID查询课程基本信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        // 如果课程基本信息为空，则返回null
        if(courseBase==null){
            return null;
        }

        // 根据课程ID查询课程市场信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        // 创建一个CourseBaseInfoDto对象
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        // 将课程基本信息复制到CourseBaseInfoDto对象中
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        if(courseMarket==null){
            return null;
        }
        // 将课程市场信息复制到CourseBaseInfoDto对象中
        BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);

        // 根据课程基本信息中的Mt字段，查询对应的分类名称并设置到CourseBaseInfoDto对象中
        courseBaseInfoDto.setMtName(courseCategoryMapper.selectById(courseBaseInfoDto.getMt()).getName());
        // 根据课程基本信息中的St字段，查询对应的分类名称并设置到CourseBaseInfoDto对象中
        courseBaseInfoDto.setStName(courseCategoryMapper.selectById(courseBaseInfoDto.getSt()).getName());

        // 返回填充了课程基本信息和市场信息的CourseBaseInfoDto对象
        return courseBaseInfoDto;

    }



    //保存课程营销信息
    private int saveCourseMarket(CourseMarket courseMarketNew){
        //收费规则
        String charge = courseMarketNew.getCharge();
        if(StringUtils.isBlank(charge)){
            throw new XueChengPlusException("收费规则没有选择");
        }
        //收费规则为收费
        if(charge.equals("201001")){
            if(courseMarketNew.getOriginalPrice()==null || courseMarketNew.getOriginalPrice().floatValue()<=0 || courseMarketNew.getPrice() == null || courseMarketNew.getPrice().floatValue()<=0){
                throw new XueChengPlusException("课程为收费价格不能为空且必须大于0");
            }
        }
        //根据id从课程营销表查询
        CourseMarket courseMarketObj = courseMarketMapper.selectById(courseMarketNew.getId());
        if(courseMarketObj == null){
            return courseMarketMapper.insert(courseMarketNew);
        }else{
            BeanUtils.copyProperties(courseMarketNew,courseMarketObj);
            courseMarketObj.setId(courseMarketNew.getId());
            return courseMarketMapper.updateById(courseMarketObj);
        }
    }
}