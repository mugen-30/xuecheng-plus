package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyChooseCourseService;
import com.xuecheng.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 */
@Service
@Slf4j
public class MyCourseTablesServiceImpl extends ServiceImpl<XcCourseTablesMapper, XcCourseTables> implements MyCourseTablesService {

    @Resource
    MyChooseCourseService myChooseCourseService;

    @Resource
    XcCourseTablesMapper xcCourseTablesMapper;

    @Resource
    ContentServiceClient contentServiceClient;



    @Override
    @Transactional
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {

        //远程调用内容管理服务查询课程收费规则
        CoursePublish coursepublish = contentServiceClient.getCoursepublishInfo(courseId);
        if (coursepublish == null) {
            log.error("查询课程发布信息失败，课程id:{}", courseId);
            XueChengPlusException.cast("课程不存在");
        }
        //收费规则
        String charge = coursepublish.getCharge();
        XcChooseCourse xcChooseCourse = null;
        if ("201000".equals(charge)) {
            //免费课程，直接添加选课记录，我的课程表记录
            xcChooseCourse = addFreeCourse(userId, coursepublish);
            addCourseTables(xcChooseCourse);
        }else {
            //收费课程，会向选课记录写入数据，我的课程表不写入数据
            xcChooseCourse = addChargeCourse(userId, coursepublish);
        }

        //查询学生的学习资格
        XcCourseTablesDto xcCourseTablesDto = getLearningStatus(userId, courseId);

        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(xcChooseCourse, xcChooseCourseDto);
        xcChooseCourseDto.setLearnStatus(xcCourseTablesDto.getLearnStatus());

        return xcChooseCourseDto;
    }

    //学习资格，[{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
    @Override
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId) {
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();

        //查询我的课程表,如果查不到，说明没有选课
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        if (xcCourseTables == null) {
            //"code":"702002","desc":"没有选课或选课后没有支付"
            xcCourseTablesDto.setCourseId(courseId);
            xcCourseTablesDto.setLearnStatus("702002");
            return xcCourseTablesDto;
        }

        //如果查到了，判断是否过期，如果过期，不能继续学习
        boolean before = xcCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now());
        BeanUtils.copyProperties(xcCourseTables, xcCourseTablesDto);
        if (before) {
            //"code":"702003","desc":"已过期需要申请续期或重新支付"
            xcCourseTablesDto.setLearnStatus("702003");
        }else {
            //"code":"702001","desc":"正常学习"
            xcCourseTablesDto.setLearnStatus("702001");
        }
        return xcCourseTablesDto;

    }

    @Transactional
    @Override
    public boolean saveChooseCourseSuccess(String chooseCourseId) {
        //根据choosecourseId查询选课记录
        XcChooseCourse xcChooseCourse = myChooseCourseService.getById(chooseCourseId);
        if(xcChooseCourse == null){
            log.debug("收到支付结果通知没有查询到关联的选课记录,choosecourseId:{}",chooseCourseId);
            return false;
        }
        String status = xcChooseCourse.getStatus();
        if("701001".equals(status)){
            //添加到课程表
            addCourseTables(xcChooseCourse);
            return true;
        }
        //待支付状态才处理
        if ("701002".equals(status)) {
            //更新为选课成功
            xcChooseCourse.setStatus("701001");
            boolean update = myChooseCourseService.updateById(xcChooseCourse);
            if(update){
                log.debug("收到支付结果通知处理成功,选课记录:{}",xcChooseCourse);
                //添加到课程表
                addCourseTables(xcChooseCourse);
                return true;
            }else{
                log.debug("收到支付结果通知处理失败,选课记录:{}",xcChooseCourse);
                return false;
            }
        }

        return false;
    }

    @Override
    public PageResult<XcCourseTables> mycoursetables( MyCourseTableParams params){
        //页码
        long pageNo = params.getPage();
        //每页记录数
        long pageSize = params.getSize();
        //分页条件
        Page<XcCourseTables> page = new Page<>(pageNo, pageSize);
        //根据用户id查询
        String userId = params.getUserId();
        LambdaQueryWrapper<XcCourseTables> lambdaQueryWrapper = new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, userId);

        //分页查询
        Page<XcCourseTables> pageResult = xcCourseTablesMapper.selectPage(page, lambdaQueryWrapper);
        List<XcCourseTables> records = pageResult.getRecords();
        //记录总数
        long total = pageResult.getTotal();
        return new PageResult<>(records, total, pageNo, pageSize);

    }

    /**
     * 添加免费课程
     * @param userId 用户id
     * @param coursepublish 课程发布信息
     * @return XcChooseCourse 选课记录
     */
    @Transactional
    public XcChooseCourse addFreeCourse(String userId, CoursePublish coursepublish) {

        //判断，如果已经选课，不能重复选课
        Long courseId = coursepublish.getId();
        List<XcChooseCourse> xcChooseCourseList = myChooseCourseService.list(
                new LambdaQueryWrapper<XcChooseCourse>()
                        .eq(XcChooseCourse::getCourseId, courseId)
                        .eq(XcChooseCourse::getOrderType, "700001")
                        .eq(XcChooseCourse::getUserId, userId));
        if (!xcChooseCourseList.isEmpty()) {
            log.error("已经选课，不能重复选课，用户id:{},课程id:{}", userId, courseId);
            return xcChooseCourseList.get(0);
        }

        //向选课记录表写入数据
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(courseId);
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700001");//免费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setValidDays(coursepublish.getValidDays());
        xcChooseCourse.setStatus("701001");//选课成功
        return getXcChooseCourse(userId, coursepublish, courseId, xcChooseCourse);
    }

    /**
     * 添加收费课程
     * @param userId 用户id
     * @param coursepublish 课程发布信息
     * @return XcChooseCourse 选课记录
     */
    @Transactional
    public XcChooseCourse addChargeCourse(String userId,CoursePublish coursepublish){

        //判断，如果存在选课记录且状态为待支付，不能重复选课
        Long courseId = coursepublish.getId();
        List<XcChooseCourse> xcChooseCourseList = myChooseCourseService.list(
                new LambdaQueryWrapper<XcChooseCourse>()
                        .eq(XcChooseCourse::getCourseId, courseId)
                        .eq(XcChooseCourse::getOrderType, "700002")//收费课程
                        .eq(XcChooseCourse::getUserId, userId));
        if (!xcChooseCourseList.isEmpty()) {
            log.error("已经选课，不能重复选课，用户id:{},课程id:{}", userId, courseId);
            return xcChooseCourseList.get(0);
        }

        //向选课记录表写入数据
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(courseId);
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700002");//收费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setValidDays(coursepublish.getValidDays());
        xcChooseCourse.setStatus("701002");//待支付
        return getXcChooseCourse(userId, coursepublish, courseId, xcChooseCourse);

    }

    @NotNull
    private XcChooseCourse getXcChooseCourse(String userId, CoursePublish coursepublish, Long courseId, XcChooseCourse xcChooseCourse) {
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(coursepublish.getValidDays()));

        boolean save = myChooseCourseService.save(xcChooseCourse);
        if (!save) {
            log.error("添加选课记录失败，用户id:{},课程id:{}", userId, courseId);
            XueChengPlusException.cast("添加选课记录失败");
        }
        return xcChooseCourse;
    }

    /**
     * 添加我的课程表
     * @param xcChooseCourse 选课记录
     * @return XcCourseTables 我的课程表
     */
    @Transactional
    public XcCourseTables addCourseTables(XcChooseCourse xcChooseCourse){

        String status = xcChooseCourse.getStatus();
        Long courseId = xcChooseCourse.getId();
        String userId = xcChooseCourse.getUserId();
        if (!"701001".equals(status)) {
            log.error("选课记录状态异常，选课记录id:{}", userId);
            XueChengPlusException.cast("选课记录状态异常");
        }
        XcCourseTables xcCourseTable1 = getXcCourseTables(userId, courseId);
        if (xcCourseTable1 != null){
            return xcCourseTable1;
        }
        XcCourseTables xcCourseTable = new XcCourseTables();
        BeanUtils.copyProperties(xcChooseCourse, xcCourseTable);
        xcCourseTable.setChooseCourseId(courseId);
        xcCourseTable.setCreateDate(LocalDateTime.now());
        xcCourseTable.setCourseType(xcChooseCourse.getOrderType());
        xcCourseTable.setUpdateDate(LocalDateTime.now());
        int save = xcCourseTablesMapper.insert(xcCourseTable);
        if (save > 0) {
            log.error("添加我的课程表失败，选课记录id:{}", userId);
            XueChengPlusException.cast("添加我的课程表失败");
        }
        return xcCourseTable;
    }

    private @Nullable XcCourseTables getXcCourseTables(String userId, Long courseId) {
        return xcCourseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>()
                .eq(XcCourseTables::getCourseId, courseId)
                .eq(XcCourseTables::getUserId, userId));
    }

}
