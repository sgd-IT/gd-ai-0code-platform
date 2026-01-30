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
        //保存css文件
        writeToFile(uniquePath, "style.css", codeResult.getCssCode());
        //保存js文件
        writeToFile(uniquePath, "script.js", codeResult.getJsCode());
    }

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    protected void validateInput(MultiFileCodeResult codeResult) {
        super.validateInput(codeResult);
        if (StrUtil.isNotBlank(codeResult.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, " HTML代码内容为空");
        }
    }
}
