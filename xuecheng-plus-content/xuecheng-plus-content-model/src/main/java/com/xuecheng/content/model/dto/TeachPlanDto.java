package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author mugen
 * @description 课程计划信息Dto
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeachPlanDto extends Teachplan {

    private TeachplanMedia teachplanMedia;

    private List<TeachPlanDto> teachPlanTreeNodes;

}
