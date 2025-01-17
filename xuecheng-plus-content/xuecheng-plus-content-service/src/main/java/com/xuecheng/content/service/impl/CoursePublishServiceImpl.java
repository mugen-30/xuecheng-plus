package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignClient.MediaServiceClient;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.*;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class CoursePublishServiceImpl extends ServiceImpl<CoursePublishMapper, CoursePublish> implements CoursePublishService {
    @Resource
    private CourseBaseInfoService courseBaseInfoService;

    @Resource
    private TeachPlanService teachPlanService;

    @Resource
    private CourseTeacherService courseTeacherService;

    @Resource
    private CourseMarketService courseMarketService;

    @Resource
    private CoursePublishPreService coursePublishPreService;

    @Resource
    private CoursePublishMapper coursePublishMapper;

    @Resource
    private MqMessageService mqMessageService;

    @Resource
    private MediaServiceClient mediaServiceClient;

    @Override
    public CoursePreviewDto getCoursePreview(Long courseId) {
        // 创建一个CoursePreviewDto对象
        CoursePreviewDto coursePreview = new CoursePreviewDto();

        // 获取课程基本信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        // 将课程基本信息设置到coursePreview对象中
        coursePreview.setCourseBase(courseBaseInfo);

        // 获取教学计划列表
        List<TeachPlanDto> teachplans = teachPlanService.queryTeachPlanTree(courseId);
        // 将教学计划列表设置到coursePreview对象中
        coursePreview.setTeachplans(teachplans);

        // 获取课程教师列表
        List<CourseTeacher> courseTeachers = courseTeacherService.queryCourseTeacherList(courseId);
        // 将课程教师列表设置到coursePreview对象中
        coursePreview.setCourseTeachers(courseTeachers);

        // 返回coursePreview对象
        return coursePreview;
    }

    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {
        CoursePreviewDto coursePreview = getCoursePreview(courseId);
        CourseBaseInfoDto courseBaseInfo = coursePreview.getCourseBase();
        List<TeachPlanDto> teachplans = coursePreview.getTeachplans();
        List<CourseTeacher> courseTeachers = coursePreview.getCourseTeachers();
        if (courseBaseInfo == null) {
            XueChengPlusException.cast("课程不存在");
        }
        if (!Objects.equals(companyId, courseBaseInfo.getCompanyId())){
            XueChengPlusException.cast("只能提交本机构的课程");
        }
        //审核状态
        String status = courseBaseInfo.getStatus();
        //如果课程状态为已提交，则不允许重复提交
        if(status.equals("202003")){
            XueChengPlusException.cast("课程已提交，不允许重复提交");
        }

        //课程的图片，课程信息是否完整
        String pic = courseBaseInfo.getPic();
        if (StringUtils.isEmpty(pic)){
            XueChengPlusException.cast("课程图片不能为空");
        }
        if (teachplans == null || teachplans.isEmpty()){
            XueChengPlusException.cast("课程计划不能为空");
        }
        if (courseTeachers == null || courseTeachers.isEmpty()){
            XueChengPlusException.cast("课程讲师不能为空");
        }

        //查询到课程基本信息，营销信息，计划，讲师等信息插入到课程预发布表
        // 创建CoursePublishPre对象
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        // 将courseBaseInfo的属性复制到coursePublishPre中
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        // 根据courseId获取对应的CourseMarket对象
        CourseMarket courseMarket = courseMarketService.getById(courseId);
        // 将courseMarket对象转换为JSON字符串
        String courseMarketJSON = JSON.toJSONString(courseMarket);
        // 将teachplans对象转换为JSON字符串
        String teachplansJSON = JSON.toJSONString(teachplans);
        // 将courseTeachers对象转换为JSON字符串
        String courseTeacherJSON = JSON.toJSONString(courseTeachers);
        // 设置coursePublishPre的market属性为courseMarketJSON
        coursePublishPre.setMarket(courseMarketJSON);
        // 设置coursePublishPre的teachplan属性为teachplansJSON
        coursePublishPre.setTeachplan(teachplansJSON);
        // 设置coursePublishPre的teachers属性为courseTeacherJSON
        coursePublishPre.setTeachers(courseTeacherJSON);
        // 设置coursePublishPre的审核状态为已提交
        coursePublishPre.setStatus("202003");
        // 设置coursePublishPre的提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        // 保存课程预发布表
        CoursePublishPre coursePublishPre1 = coursePublishPreService.getById(courseId);
        if (coursePublishPre1 == null){
            coursePublishPreService.save(coursePublishPre);
        } else {
            coursePublishPreService.updateById(coursePublishPre);
        }

        //更新课程基本信息表的审核状态为已提交
        CourseBase courseBase = courseBaseInfoService.getById(courseId);
        courseBase.setAuditStatus("202003");
        courseBaseInfoService.updateById(courseBase);

    }

    @Override
    public void publish(Long companyId, Long courseId) {

        CoursePublishPre coursePublishPre = coursePublishPreService.getById(courseId);
        if (coursePublishPre == null){
            XueChengPlusException.cast("课程没有审核记录，无法发布");
        }
        // 审核状态
        String status = coursePublishPre.getStatus();
        if (!status.equals("202004")){
            XueChengPlusException.cast("课程审核未通过，无法发布");
        }
        // 向课程发布表写入数据
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        coursePublish.setStatus("203002");
        // 先查询发布表，有则更新，没有则插入
        CoursePublish coursePublishObj = coursePublishMapper.selectById(courseId);
        if (coursePublishObj == null){
            coursePublishMapper.insert(coursePublish);
        }else {
            coursePublishMapper.updateById(coursePublish);
        }
        //更新课程基本表的发布状态
        CourseBase courseBase = courseBaseInfoService.getById(courseId);
        courseBase.setStatus("203002");
        courseBaseInfoService.updateById(courseBase);

        // 向消息表写入数据
        saveCoursePublishMessage(courseId);

        // 将课程预发布表数据删除
        coursePublishPreService.removeById(courseId);

    }

    @Override
    public File generateCourseHtml(Long courseId) {
        //静态化文件
        File htmlFile  = null;

        try {
            //配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            //加载模板
            //选指定模板路径,classpath下templates下
            //得到classpath路径
            String classpath = URLDecoder.decode(this.getClass().getResource("/").getPath(), "utf-8");
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //设置字符编码
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");

            //准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreview(courseId);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //静态化
            //参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
//            System.out.println(content);
            //将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            //创建静态化文件
            htmlFile = File.createTempFile("course",".html");
            log.debug("课程静态化，生成静态文件:{}",htmlFile.getAbsolutePath());
            //输出流
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            log.error("课程静态化异常:课程id:{}，异常信息：{}", courseId, e.toString());
            XueChengPlusException.cast("课程静态化异常");
        }

        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course = mediaServiceClient.upload(multipartFile, "course/"+courseId+".html");
        if(course==null){
            log.debug("上传静态文件异常:课程id:{}，异常信息：", courseId);
            XueChengPlusException.cast("上传静态文件异常");
        }
    }

    @Override
    public void offline(Long companyId, Long courseId) {
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        if (coursePublish == null){
            XueChengPlusException.cast("课程不存在");
        }
        // 更新课程发布表的状态为已下线
        coursePublish.setStatus("203003");
        coursePublishMapper.updateById(coursePublish);

        // 更新课程基本表的状态为已下线
        CourseBase courseBase = courseBaseInfoService.getById(courseId);
        courseBase.setStatus("203003");
        courseBaseInfoService.updateById(courseBase);

        // 向消息表写入数据
        saveCourseOffLineMessage(courseId);
    }

    @Override
    public void deleteCourseHtml(Long courseId) {
        mediaServiceClient.delete("course/"+courseId+".html");
    }

    @Override
    public CoursePreviewDto getPublishCoursePreview(Long courseId) {
        CoursePreviewDto coursePreview = new CoursePreviewDto();
        CoursePublish coursePublish = getById(courseId);
        if (coursePublish == null) {
            XueChengPlusException.cast("课程不存在");
        }
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(coursePublish, courseBaseInfoDto);
        //课程计划
        String teachplan = coursePublish.getTeachplan();
        String teacher = coursePublish.getTeachers();
        //转换为List
        List<TeachPlanDto> teachPlanDtos = JSON.parseArray(teachplan, TeachPlanDto.class);
        List<CourseTeacher> courseTeachers = JSON.parseArray(teacher, CourseTeacher.class);
        //封装数据
        coursePreview.setCourseBase(courseBaseInfoDto);
        coursePreview.setTeachplans(teachPlanDtos);
        coursePreview.setCourseTeachers(courseTeachers);
        return coursePreview;
    }

    /**
     * 保存课程下架消息
     * @param courseId 课程ID
     * @throws XueChengPlusException 如果消息添加失败，则抛出XueChengPlusException异常
     */
    private void saveCourseOffLineMessage(Long courseId) {

        MqMessage message = mqMessageService.addMessage("course_offline",
                String.valueOf(courseId), null, null);
        if (message == null) {
            XueChengPlusException.cast(CommonError.UNKNOWN_ERROR);
        }

    }

    /**
     * 保存课程发布消息
     * @param courseId 课程ID
     * @throws XueChengPlusException 如果消息添加失败，则抛出XueChengPlusException异常
     */
    private void saveCoursePublishMessage(Long courseId) {
        MqMessage message = mqMessageService.addMessage("course_publish",
                String.valueOf(courseId), null, null);
        if (message == null) {
            XueChengPlusException.cast(CommonError.UNKNOWN_ERROR);
        }
    }
}
