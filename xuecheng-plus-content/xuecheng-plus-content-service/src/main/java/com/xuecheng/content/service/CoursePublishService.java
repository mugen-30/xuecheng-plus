package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;

public interface CoursePublishService extends IService<CoursePublish> {

    /**
     * 获取课程预览信息
     * @param courseId 课程ID
     * @return 课程预览信息
     */
    CoursePreviewDto getCoursePreview(Long courseId);

    /**
     * @description 提交审核
     * @param courseId  课程id
     */
    public void commitAudit(Long companyId,Long courseId);

    /**
     * @description 课程发布接口
     * @param companyId 机构id
     * @param courseId 课程id
     */
    public void publish(Long companyId,Long courseId);
}
