package com.edu.teaching;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author wuming
 * @description: 面向计算机专业的个性化交互式智能教学系统服务端启动入口
 */
@SpringBootApplication
@Slf4j
@MapperScan("com.edu.teaching.**.mapper")
@EnableScheduling
public class IntelligentTeachingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntelligentTeachingSystemApplication.class, args);
        log.info("JAVA后端服务启动成功！！！！！！");
    }

}
