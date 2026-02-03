package com.company.core;

import com.company.ai.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;


@SpringBootTest
class AiCodeGeneratorFacadeTest {
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    private static final Long appId = 1L;

    @Test
    void generateAndSaveCode() throws Exception {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("做一个自我介绍页面，20行", CodeGenTypeEnum.MULTI_FILE,appId);
        Assertions.assertNotNull( file);
    }

    @Test
    void generateAndSaveCodeStream() {
        Flux<String>  htmlcode= aiCodeGeneratorFacade.generateAndSaveCodeStream("做一个自我介绍页面，20行", CodeGenTypeEnum.HTML, appId);
        List<String> block = htmlcode.collectList().block();

        Assertions.assertNotNull(block);
        String join = String.join("", block);
        Assertions.assertNotNull( join);
    }
}