package com.company.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员更新应用请求（支持更新应用名称、应用封面、优先级）
 *
 * @author gd
 */
@Data
public class AppAdminUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用封面
     */
    private String cover;

    /**
     * 优先级
     */
    private Integer priority;

    private static final long serialVersionUID = 1L;
}
