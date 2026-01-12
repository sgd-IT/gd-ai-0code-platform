package com.company.ai.model.enums;

import cn.hutool.core.util.ObjUtil;

import lombok.Getter;

@Getter
public enum CodenGenTypeEnum {
    HTML("原生HTML模式","html"),
    MULTI_FILE("多文件模式","multiFile");

    private final String text;
    private final String value;

    CodenGenTypeEnum(String text,String value){
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举
     * @param value
     * @return
     */
    public static CodenGenTypeEnum getEnumByValue(String value){
        if(ObjUtil.isEmpty(value)){
            return null;
        }
        for(CodenGenTypeEnum codenGenTypeEnum:CodenGenTypeEnum.values()){
            if (codenGenTypeEnum.value.equals(value)){
                return codenGenTypeEnum;
            }
        }
        return null;
    }
}
