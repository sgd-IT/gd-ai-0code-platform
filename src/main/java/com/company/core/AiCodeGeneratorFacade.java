package com.company.core;

import com.company.ai.AiCodeGeneratorService;
import com.company.ai.model.HtmlCodeResult;
import com.company.ai.model.MultiFileCodeResult;
import com.company.ai.model.enums.CodenGenTypeEnum;
import com.company.exception.BusinessException;
import com.company.exception.ErrorCode;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 生成代码并保存
     * @param userMessage
     * @param codenGenTypeEnum
     * @return
     */
    public File generateAndSaveCode(String userMessage, CodenGenTypeEnum  codenGenTypeEnum) {
        if(codenGenTypeEnum == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"生成类型为空");
        }
        return switch (codenGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,"不支持的生成类型"+codenGenTypeEnum.getValue());
        };
    }

    /**
     * 生成HTML代码并保存
     * @param userMessage
     * @return
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult htmlCodeRequest = aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlCodeResult(htmlCodeRequest);
    }

    /**
     * 生成多文件代码并保存
     * @param userMessage
     * @return
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult multiFileCodeRequest = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMultiFileCodeResult(multiFileCodeRequest);
    }

}
