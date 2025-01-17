package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.service.MyChooseCourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 */
@Service
@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
public class MyChooseCourseServiceImpl extends ServiceImpl<XcChooseCourseMapper, XcChooseCourse> implements MyChooseCourseService {



}
