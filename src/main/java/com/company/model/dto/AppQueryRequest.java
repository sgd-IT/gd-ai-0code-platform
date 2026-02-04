package com.company.model.dto;

import com.company.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户查询应用请求（支持根据名称查询，每页最多20个）
 *
 * @author gd
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppQueryRequest extends PageRequest implements Serializable {

    /**
     * 应用名称（模糊搜索）
     */
    private String appName;

    /**
     * 优先级
     */
    private Integer priority;

    private static final long serialVersionUID = 1L;
}
