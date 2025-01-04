package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.service.TeachPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api(value = "课程计划管理接口", tags = "课程计划管理接口")
@RestController
public class TeachPlanController {

    @Resource
    private TeachPlanService teachPlanService;

    @ApiOperation(value = "查询课程计划树结构")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachPlanDto> getTreeNodes(@PathVariable Long courseId) {
        return teachPlanService.queryTeachPlanTree(courseId);
    }

    @ApiOperation(value = "添加或修改课程计划")
    @PostMapping("/teachplan")
    public void saveTeachPlan(@RequestBody @Validated SaveTeachplanDto saveTeachplanDto) {
        teachPlanService.saveTeachPlan(saveTeachplanDto);
    }

    @ApiOperation(value = "删除课程计划")
    @DeleteMapping("/teachplan/{id}")
    public void deleteTeachPlan(@PathVariable Long id) {
        teachPlanService.deleteTeachPlan(id);
    }

    @ApiOperation(value = "上移课程计划")
    @PostMapping("/teachplan/moveup/{id}")
    public void moveUp(@PathVariable Long id) {
        teachPlanService.moveUp(id);
    }

    @ApiOperation(value = "下移课程计划")
    @PostMapping("/teachplan/movedown/{id}")
    public void moveDown(@PathVariable Long id) {
        teachPlanService.moveDown(id);
    }

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto) {
        teachPlanService.associationMedia(bindTeachplanMediaDto);
    }

    @ApiOperation("解绑课程计划和媒资")
    @DeleteMapping("/teachplan/association/media/{teachPlanId}/{mediaId}")
    public void deleteTeachplanMedia(@PathVariable Long teachPlanId, @PathVariable String mediaId) {
        teachPlanService.deleteTeachplanMedia(teachPlanId, mediaId);
    }


}
