package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import lombok.Data;

import java.util.List;

/**
 * @author mugen
 * @description 课程分类树Dto
 */
@Data
public class CourseCategoryTreeDto extends CourseCategory {

    List<CourseCategoryTreeDto> childrenTreeNodes;

}
