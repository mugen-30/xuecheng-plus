package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;

import java.util.List;

public interface TeachPlanService {

    /**
     * 查询课程计划树
     * @param courseId 课程ID
     * @return 课程计划树
     */
    public List<TeachPlanDto> queryTeachPlanTree(Long courseId);

    /**
     * 保存课程计划树
     * @param saveTeachplanDto 课程计划树
     */
    public void saveTeachPlan(SaveTeachplanDto  saveTeachplanDto);

    /**
     * 删除课程计划章节
     * @param id 课程计划ID
     */
    public void deleteTeachPlan(Long id);

    /**
     * 上移课程计划章节
     * @param id 课程计划ID
     */
    public void moveUp(Long id);

    /**
     * 下移课程计划章节
     * @param id 课程计划ID
     */
    public void moveDown(Long id);

}
