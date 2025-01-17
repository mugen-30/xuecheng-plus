package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;

import java.util.List;

public interface TeachPlanService extends IService<Teachplan> {

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

    /**
     * @description 教学计划绑定媒资
     * @param bindTeachplanMediaDto
     * @return com.xuecheng.content.model.po.TeachplanMedia
     */
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    /**
     * @description 删除教学计划绑定的媒资
     * @param teachPlanId 教学计划ID
     * @param mediaId 媒体资源ID
     */
    void deleteTeachplanMedia(Long teachPlanId, String mediaId);
}
