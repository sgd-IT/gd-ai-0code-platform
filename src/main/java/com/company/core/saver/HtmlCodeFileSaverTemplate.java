package com.company.core.saver;

import cn.hutool.core.util.StrUtil;
import com.company.ai.model.HtmlCodeResult;
import com.company.ai.model.enums.CodeGenTypeEnum;
import com.company.exception.BusinessException;
import com.company.exception.ErrorCode;

public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {

    @Override
    protected void saveFiles(String uniquePath, HtmlCodeResult codeResult) {
        //保存html文件
        writeToFile(uniquePath,"index.html", codeResult.getHtmlCode());
    }

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    protected void validateInput(HtmlCodeResult codeResult) {
        super.validateInput(codeResult);
        if (StrUtil.isBlank(codeResult.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, " Html代码内容为空");
        }
    }
}
