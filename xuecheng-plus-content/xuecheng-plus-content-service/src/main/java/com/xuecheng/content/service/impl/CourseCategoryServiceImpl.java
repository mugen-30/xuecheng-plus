package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 课程分类 服务实现类
 * </p>
 *
 * @author mugen
 */
@Slf4j
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Resource
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {

        // 从数据库中查询与给定id相关的所有课程分类树节点
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);

        // 构建一个映射，将节点的id作为键，节点对象作为值
        // 过滤掉id与给定id相同的节点，避免根节点被错误地添加到映射中
        Map<String, CourseCategoryTreeDto> map =
                courseCategoryTreeDtos.stream().filter(item -> !id.equals(item.getId()))
                        .collect(Collectors.toMap(CourseCategory::getId, value -> value, (key1, key2) -> key1));

        // 初始化一个空列表，用于存储与给定id直接相关的节点（即parentid等于给定id的节点）
        List<CourseCategoryTreeDto> courseCategoryList = new ArrayList<>();

        // 遍历查询结果中的每个节点
        courseCategoryTreeDtos.forEach(item -> {
            // 如果节点的parentid等于给定的id，则将该节点添加到courseCategoryList列表中
            if (item.getParentid().equals(id)) {
                courseCategoryList.add(item);
            }
            // 从映射中获取当前节点的父节点对象
            CourseCategoryTreeDto courseCategoryTreeDto = map.get(item.getParentid());
            // 如果找到了父节点对象
            if (courseCategoryTreeDto != null) {
                // 获取父节点的子节点列表
                List<CourseCategoryTreeDto> children = courseCategoryTreeDto.getChildrenTreeNodes();
                // 如果子节点列表为空，则初始化一个新列表
                if (children == null) {
                    children = new ArrayList<>();
                }
                // 将当前节点添加到父节点的子节点列表中
                children.add(item);
                // 更新父节点的子节点列表
                courseCategoryTreeDto.setChildrenTreeNodes(children);
            }
        });

        // 返回与给定id直接相关的节点列表
        return courseCategoryList;
    }
}
