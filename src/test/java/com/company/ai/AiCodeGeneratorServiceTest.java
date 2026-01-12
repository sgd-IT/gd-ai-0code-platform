package com.company.ai;

import com.company.ai.model.HtmlCodeResult;
import com.company.ai.model.MultiFileCodeResult;
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
    void aiCodeGeneratorHtmlService() {
        HtmlCodeResult htmlCodeRequest = aiCodeGeneratorService.generateHtmlCode("请生成一个导航栏HTML代码,20行");
        Assertions.assertNotNull(htmlCodeRequest);
    }

    @Test
    void aiCodeGeneratorMultifileService() {
        MultiFileCodeResult multiFileCodeRequest = aiCodeGeneratorService.generateMultiFileCode("请生成一个导航栏代码,20行");
        Assertions.assertNotNull(multiFileCodeRequest);
    }
}