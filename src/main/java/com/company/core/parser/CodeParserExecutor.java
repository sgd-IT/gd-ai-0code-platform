package com.company.core.parser;

import com.company.ai.model.enums.CodeGenTypeEnum;
import com.company.exception.BusinessException;
import com.company.exception.ErrorCode;

public class CodeParserExecutor {
    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();
    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();

    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML -> htmlCodeParser.parse(codeContent);
            case MULTI_FILE -> multiFileCodeParser.parse(codeContent);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的生成类型" + codeGenTypeEnum.getValue());
        };
    }
}
