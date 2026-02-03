package com.company.service;

import com.company.model.dto.AppAdminQueryRequest;
import com.company.model.dto.AppQueryRequest;
import com.company.model.entity.App;
import com.company.model.entity.User;
import com.company.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author gd
 */
public interface AppService extends IService<App> {

    /**
     * 应用聊天生成代码（流式SSE）
     * @param message
     * @param appId
     * @param loginUser
     * @return
     */
    Flux<String> chatToGenCode(String message, Long appId, User loginUser);


    /**
     * 校验应用数据（创建时）
     *
     * @param app 应用
     * @param add 是否为创建校验
     */
    void validApp(App app, boolean add);

    /**
     * 获取应用视图对象
     *
     * @param app 应用
     * @return 应用视图对象
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用视图对象列表
     *
     * @param appList 应用列表
     * @return 应用视图对象列表
     */
    List<AppVO> getListAppVO(List<App> appList);

    /**
     * 获取用户查询条件（支持根据名称查询）
     *
     * @param appQueryRequest 查询请求
     * @param userId          用户id
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest, Long userId);

    /**
     * 获取精选应用查询条件（支持根据名称查询）
     *
     * @param appQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getFeaturedQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取管理员查询条件（支持根据除时间外的任何字段查询）
     *
     * @param appAdminQueryRequest 管理员查询请求
     * @return 查询条件
     */
    QueryWrapper getAdminQueryWrapper(AppAdminQueryRequest appAdminQueryRequest);
}
