package com.company.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void aiCodeGeneratorService() {
        String result = aiCodeGeneratorService.generateHtmlCode("请生成一个导航栏HTML代码,20行");
        Assertions.assertNotNull(result);
    }
}