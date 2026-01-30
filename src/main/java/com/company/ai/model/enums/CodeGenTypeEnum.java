package com.company.ai.model.enums;

import cn.hutool.core.util.ObjUtil;

import lombok.Getter;

@Getter
public enum CodeGenTypeEnum {
    HTML("原生HTML模式","html"),
    MULTI_FILE("多文件模式","multiFile");

    private final String text;
    private final String value;

    CodeGenTypeEnum(String text, String value){
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举
     * @param value
     * @return
     */
    public static CodeGenTypeEnum getEnumByValue(String value){
        if(ObjUtil.isEmpty(value)){
            return null;
        }
        for(CodeGenTypeEnum codeGenTypeEnum : CodeGenTypeEnum.values()){
            if (codeGenTypeEnum.value.equals(value)){
                return codeGenTypeEnum;
            }
        }
        return null;
    }
}
