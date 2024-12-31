package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.TeachPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TeachPlanServiceImpl implements TeachPlanService {

    @Resource
    private TeachplanMapper teachplanMapper;

    @Override
    public List<TeachPlanDto> queryTeachPlanTree(Long courseId) {

        return teachplanMapper.selectTreeNodes(courseId);

    }

    @Transactional
    @Override
    public void saveTeachPlan(SaveTeachplanDto saveTeachplanDto) {

        //通过课程计划id判断是新增还是修改
        Long teachplanId = saveTeachplanDto.getId();
        if (teachplanId == null){
            //新增
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);

            //确定排序字段
            Long courseId = saveTeachplanDto.getCourseId();
            Long parentId = saveTeachplanDto.getParentid();
            int order = getTeachPlanOrder(courseId, parentId);
            teachplan.setOrderby(order);

            teachplanMapper.insert(teachplan);
        }else{
            //修改
            Teachplan teachplan = BeanUtils.instantiateClass(Teachplan.class);
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);
            teachplanMapper.updateById(teachplan);
        }


    }

    @Transactional
    @Override
    public void deleteTeachPlan(Long id) {

        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getId,id)
                .or().eq(Teachplan::getParentid,id);
        teachplanMapper.delete(queryWrapper);

    }

    @Transactional
    @Override
    public void moveUp(Long id) {

        Teachplan teachplan = teachplanMapper.selectById(id);

        Long courseId = teachplan.getCourseId();
        Long parentid = teachplan.getParentid();

        //查询出上一个节点的id
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId)
                .eq(Teachplan::getParentid, parentid)
                .lt(Teachplan::getOrderby, teachplan.getOrderby())
                .orderByDesc(Teachplan::getOrderby);
        List<Teachplan> list = teachplanMapper.selectList(queryWrapper);
        Teachplan pre = null;
        if (list.isEmpty()) {
            XueChengPlusException.cast("已经是第一个章节");
        } else {
            pre = list.get(0);
        }

        //交换两个节点的排序字段
        exchangeOrder(teachplan, pre);

    }

    @Transactional
    @Override
    public void moveDown(Long id) {

        Teachplan teachplan = teachplanMapper.selectById(id);

        Long courseId = teachplan.getCourseId();
        Long parentid = teachplan.getParentid();

        //查询出下一个节点的id
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId)
                .eq(Teachplan::getParentid, parentid)
                .gt(Teachplan::getOrderby, teachplan.getOrderby())
                .orderByAsc(Teachplan::getOrderby);
        List<Teachplan> list = teachplanMapper.selectList(queryWrapper);
        Teachplan next = null;
        if (list.isEmpty()) {
            XueChengPlusException.cast("已经是最后一个章节");
        } else {
            next = list.get(0);
        }

        //交换两个节点的排序字段
        exchangeOrder(teachplan, next);

    }

    private int getTeachPlanOrder(Long courseId, Long parentid) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,courseId)
                .eq(Teachplan::getParentid,parentid)
                .orderByDesc(Teachplan::getOrderby);
        List<Teachplan> list = teachplanMapper.selectList(queryWrapper);
        if (list.isEmpty()){
            return 1;
        }
        Integer orderby = list.get(0).getOrderby();
        return orderby+1;
    }

    private void exchangeOrder(Teachplan t1, Teachplan t2) {
        Integer orderby1 = t1.getOrderby();
        Integer orderby2 = t2.getOrderby();
        t1.setOrderby(orderby2);
        t2.setOrderby(orderby1);
        teachplanMapper.updateById(t1);
        teachplanMapper.updateById(t2);
    }
}
