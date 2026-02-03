package com.company.core.saver;

import com.company.ai.model.HtmlCodeResult;
import com.company.ai.model.MultiFileCodeResult;
import com.company.ai.model.enums.CodeGenTypeEnum;
import com.company.exception.BusinessException;
import com.company.exception.ErrorCode;

import java.io.File;

/**
 * 代码文件保存执行器
 * 根据saver包下的各个类型保存方法执行
 */
public class CodeFileSaverExecutor {
    private static final HtmlCodeFileSaverTemplate htmlCodeFileSaverTemplate = new HtmlCodeFileSaverTemplate();

    private static final MutilFileSaverTemplate mutilFileSaverTemplate = new MutilFileSaverTemplate();

    //执行代码保存方法
    public static File executeSaver(CodeGenTypeEnum codeType, Object codeResult, Long appId) {
        return switch (codeType){
            case HTML -> htmlCodeFileSaverTemplate.saveCode((HtmlCodeResult) codeResult, appId);
            case MULTI_FILE -> mutilFileSaverTemplate.saveCode((MultiFileCodeResult) codeResult, appId);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的生成类型" + codeType.getValue());
        };
    }
}
