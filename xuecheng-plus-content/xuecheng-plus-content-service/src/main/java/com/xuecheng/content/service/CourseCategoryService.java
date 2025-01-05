package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

public interface CourseCategoryService {

    /**
     * 查询课程分类树
     * @param id 课程分类id
     * @return
     */
    public List<CourseCategoryTreeDto> queryTreeNodes(String id);

}
