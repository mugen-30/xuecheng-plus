package com.xuecheng.learning.model.dto;

import com.xuecheng.learning.model.po.XcChooseCourse;
import lombok.Data;
import lombok.ToString;

/**
 * @author mugen
 * @description 选课信息
 */
@Data
@ToString
public class XcChooseCourseDto extends XcChooseCourse {

    //学习资格，[{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
    public String learnStatus;

}
