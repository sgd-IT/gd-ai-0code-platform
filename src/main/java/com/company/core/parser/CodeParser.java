package com.company.core.parser;

/**
 * 代码解析器策略接口
 * @param <T>
 */
public interface CodeParser<T> {

    /**
     * 解析代码内容
     *
     * @param codeContent 原始代码内容
     * @return
     */
    T parse(String codeContent);
}
