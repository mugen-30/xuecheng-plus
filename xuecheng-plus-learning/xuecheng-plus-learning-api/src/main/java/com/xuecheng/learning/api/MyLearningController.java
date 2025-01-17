package com.xuecheng.learning.api;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author mugen
 * @description 我的学习接口
 */
@Api(value = "学习过程管理接口", tags = "学习过程管理接口")
@Slf4j
@RestController
public class MyLearningController {

    @Resource
    private LearningService learningService;


    @ApiOperation("获取视频")
    @GetMapping("/open/learn/getvideo/{courseId}/{teachplanId}/{mediaId}")
    public RestResponse<String> getvideo(@PathVariable("courseId") Long courseId,
                                         @PathVariable("teachplanId") Long teachplanId,
                                         @PathVariable("mediaId") String mediaId) {

        SecurityUtil.XcUser user = SecurityUtil.getUser();
        String userId;
        if (user == null) {
            userId = "";
        } else {
            userId = user.getId();
        }
        return learningService.getVideo(userId, courseId, teachplanId, mediaId);

    }

}
