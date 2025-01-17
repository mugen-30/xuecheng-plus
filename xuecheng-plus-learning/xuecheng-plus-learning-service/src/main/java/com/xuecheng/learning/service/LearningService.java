package com.xuecheng.learning.service;

import com.xuecheng.base.model.RestResponse;

/**
 * @description 学习相关服务
 * @Author mugen
 **/
public interface LearningService {

    /**
     * 获取视频
     * @param userId 用户ID
     * @param courseId 课程ID
     * @param teachplanId 计划ID
     * @param mediaId 媒体ID
     * @return 视频地址
     */
    RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId);

}
