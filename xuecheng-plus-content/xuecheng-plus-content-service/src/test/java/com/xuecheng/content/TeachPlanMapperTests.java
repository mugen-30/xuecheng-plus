package com.xuecheng.content;

import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.TeachPlanDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class TeachPlanMapperTests {

    @Autowired
    private TeachplanMapper teachPlanMapper;

    @Test
    void testSelectTreeNodes() {
        List<TeachPlanDto> teachPlanDtos = teachPlanMapper.selectTreeNodes(117L);
        System.out.println(teachPlanDtos);
    }

}
