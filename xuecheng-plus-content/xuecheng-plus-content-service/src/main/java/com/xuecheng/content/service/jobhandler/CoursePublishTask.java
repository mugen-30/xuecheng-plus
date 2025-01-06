package com.xuecheng.content.service.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.feignClient.CourseIndex;
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
import java.io.File;

@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract {

    @Resource
    private MqMessageService mqMessageService;

    @Resource
    private CoursePublishService coursePublishService;

    @Resource
    private SearchServiceClient searchServiceClient;

    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception{

        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex={},shardTotal={}", shardIndex, shardTotal);
        process(shardIndex, shardTotal, "course_publish", 30, 60);

    }

    @Override
    public boolean execute(MqMessage mqMessage) {
        //课程id
        Long courseId = Long.valueOf(mqMessage.getBusinessKey1());

        //课程静态化上传到minio
        generateCourseHtml(mqMessage,courseId);

        //向elasticsearch中写入数据
        saveCourseIndex(mqMessage,courseId);

        //向redis中写入数据
//        int i = 1/0;

        return true;
    }

    private void generateCourseHtml (MqMessage mqMessage, Long courseId) {
        //判断任务状态
        Long taskId = mqMessage.getId();
        int stageOne = mqMessageService.getStageOne(taskId);
        if (stageOne > 0) {
            log.debug("课程静态化已完成，无需再次执行");
            return;
        }
        //开始静态化
        File file = coursePublishService.generateCourseHtml(courseId);
        //上传静态化文件到minio
        if(file!=null){
            coursePublishService.uploadCourseHtml(courseId,file);
        }

        //任务处理完成，更新状态
        mqMessageService.completedStageOne(taskId);

    }

    private void saveCourseIndex (MqMessage mqMessage, Long courseId) {
        //判断任务状态
        Long taskId = mqMessage.getId();
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if (stageTwo > 0) {
            log.debug("课程索引写入已完成，无需再次执行");
            return;
        }
        //查询课程信息，调用搜索服务添加索引
        CoursePublish coursePublish = coursePublishService.getById(courseId);
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish, courseIndex);
        //调用搜索服务添加索引
        Boolean add = searchServiceClient.add(courseIndex);
        if (!add){
            log.error("课程索引写入失败，索引信息：{}", courseIndex);
            XueChengPlusException.cast("课程索引写入失败");
        }


        //任务处理完成，更新状态
        mqMessageService.completedStageTwo(taskId);

    }

}
