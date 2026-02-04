package com.company.core.saver;

import cn.hutool.core.util.StrUtil;
import com.company.ai.model.MultiFileCodeResult;
import com.company.ai.model.enums.CodeGenTypeEnum;
import com.company.exception.BusinessException;
import com.company.exception.ErrorCode;

public class MutilFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult>{
    @Override
    protected void saveFiles(String uniquePath, MultiFileCodeResult codeResult) {
        //保存html文件
        writeToFile(uniquePath, "index.html", codeResult.getHtmlCode());
        //保存css文件（如果为空则创建默认文件）
        String cssCode = codeResult.getCssCode();
        if (StrUtil.isBlank(cssCode)) {
            cssCode = "/* 样式文件 */\n";
        }
        writeToFile(uniquePath, "style.css", cssCode);
        //保存js文件（如果为空则创建默认文件）
        String jsCode = codeResult.getJsCode();
        if (StrUtil.isBlank(jsCode)) {
            jsCode = "// JavaScript 代码文件\n";
        }
        writeToFile(uniquePath, "script.js", jsCode);
    }

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    protected void validateInput(MultiFileCodeResult codeResult) {
        super.validateInput(codeResult);
        if (StrUtil.isBlank(codeResult.getHtmlCode())) {  // 为空时才抛异常
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码内容为空");
        }
    }
}
