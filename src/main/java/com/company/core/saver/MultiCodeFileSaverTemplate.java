package com.company.core.saver;

import com.company.ai.model.enums.CodeGenTypeEnum;

public class MultiCodeFileSaverTemplate extends CodeFileSaverTemplate{
    @Override
    protected void saveFiles(String uniquePath, Object codeResult) {

    }

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return null;
    }
}
