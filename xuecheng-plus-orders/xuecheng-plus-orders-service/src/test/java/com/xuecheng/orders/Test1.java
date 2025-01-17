package com.xuecheng.orders;

import cn.hutool.core.util.IdUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Test1 {


  @Test
 public void test(){
      long snowflakeNextId = IdUtil.getSnowflakeNextId();
      System.out.println(snowflakeNextId);
      long snowflakeNextId1 = IdUtil.getSnowflakeNextId();
      System.out.println(snowflakeNextId1);
  }

}
