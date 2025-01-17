package com.xuecheng.content.service.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.feignClient.CourseIndex;
import com.xuecheng.content.feignClient.MediaServiceClient;
import com.xuecheng.content.feignClient.SearchServiceClient;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class CourseOffLineTask extends MessageProcessAbstract {

    @Resource
    private MqMessageService mqMessageService;

    @Resource
    private CoursePublishService coursePublishService;

    @Resource
    private SearchServiceClient searchServiceClient;

    @Resource
    private MediaServiceClient mediaServiceClient;

    @XxlJob("CourseOffLineJobHandler")
    public void courseOffLineJobHandler() throws Exception{

        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex={},shardTotal={}", shardIndex, shardTotal);
        process(shardIndex, shardTotal, "course_offline", 30, 60);

    }

    @Override
    public boolean execute(MqMessage mqMessage) {
        //课程id
        Long courseId = Long.valueOf(mqMessage.getBusinessKey1());

        //删除minio的课程静态化文件
        deleteCourseHtml(mqMessage,courseId);

        //删除elasticsearch中的索引
        deleteCourseIndex(mqMessage,courseId);

        //删除redis中的数据
//        int i = 1/0;

        return true;
    }

    private void deleteCourseHtml (MqMessage mqMessage, Long courseId) {
        //判断任务状态
        Long taskId = mqMessage.getId();
        int stageOne = mqMessageService.getStageOne(taskId);
        if (stageOne > 0) {
            log.debug("删除课程静态文件已完成，无需再次执行");
            return;
        }

        //删除minio的课程静态化文件
//        mediaServiceClient.delete(courseId.toString());//不删除

        //任务处理完成，更新状态
        mqMessageService.completedStageOne(taskId);

    }

    private void deleteCourseIndex (MqMessage mqMessage, Long courseId) {
        //判断任务状态
        Long taskId = mqMessage.getId();
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if (stageTwo > 0) {
            log.debug("课程索引删除已完成，无需再次执行");
            return;
        }
        //查询课程信息，调用搜索服务添加索引
        CoursePublish coursePublish = coursePublishService.getById(courseId);
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish, courseIndex);
        //调用搜索服务添加索引
        Boolean delete = searchServiceClient.delete(courseIndex);
        if (!delete){
            log.error("课程索引删除失败，索引信息：{}", courseIndex);
            XueChengPlusException.cast("课程索引删除失败");
        }


        //任务处理完成，更新状态
        mqMessageService.completedStageTwo(taskId);

    }

}
