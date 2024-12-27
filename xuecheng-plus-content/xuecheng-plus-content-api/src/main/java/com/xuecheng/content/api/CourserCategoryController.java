package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Api(value = "课程分类接口", tags = "课程分类接口")
@RestController
public class CourserCategoryController {

    @Resource
    private CourseCategoryService courseCategoryService;

    @ApiOperation(value = "课程分类树形结构查询")
    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> getCourseCategoryTreeNodes() {
        return courseCategoryService.queryTreeNodes("1");
    }

}
