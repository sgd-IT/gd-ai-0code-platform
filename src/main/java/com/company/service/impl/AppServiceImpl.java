package com.company.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.company.exception.BusinessException;
import com.company.exception.ErrorCode;
import com.company.mapper.AppMapper;
import com.company.model.dto.AppAdminQueryRequest;
import com.company.model.dto.AppQueryRequest;
import com.company.model.entity.App;
import com.company.model.entity.User;
import com.company.model.vo.AppVO;
import com.company.model.vo.UserVO;
import com.company.service.AppService;
import com.company.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author gd
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    /**
     * 校验应用数据
     *
     * @param app 应用
     * @param add 是否为创建校验
     */
    @Override
    public void validApp(App app, boolean add) {
        if (app == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        String appName = app.getAppName();
        String initPrompt = app.getInitPrompt();

        // 创建时必须填写 initPrompt
        if (add) {
            if (StrUtil.isBlank(initPrompt)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "initPrompt 不能为空");
            }
        }

        // 通用校验
        if (StrUtil.isNotBlank(appName) && appName.length() > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用名称过长");
        }
        if (StrUtil.isNotBlank(initPrompt) && initPrompt.length() > 10000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "initPrompt 内容过长");
        }
    }

    /**
     * 获取应用视图对象
     *
     * @param app 应用
     * @return 应用视图对象
     */
    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);

        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }

        return appVO;
    }

    /**
     * 获取应用视图对象列表
     *
     * @param appList 应用列表
     * @return 应用视图对象列表
     */
    @Override
    public List<AppVO> getListAppVO(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }

        // 批量获取用户信息
        Set<Long> userIdSet = appList.stream()
                .map(App::getUserId)
                .filter(userId -> userId != null && userId > 0)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        return appList.stream()
                .map(app -> {
                    AppVO appVO = new AppVO();
                    BeanUtil.copyProperties(app, appVO);
                    Long userId = app.getUserId();
                    if (userId != null && userMap.containsKey(userId)) {
                        UserVO userVO = userService.getUserVO(userMap.get(userId));
                        appVO.setUser(userVO);
                    }
                    return appVO;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取用户查询条件（支持根据名称查询）
     *
     * @param appQueryRequest 查询请求
     * @param userId          用户id
     * @return 查询条件
     */
    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest, Long userId) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        String appName = appQueryRequest.getAppName();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        return QueryWrapper.create()
                .eq("userId", userId)
                .like("appName", appName)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    /**
     * 获取精选应用查询条件（支持根据名称查询）
     * 精选应用：priority > 0 的应用，按优先级降序排列
     *
     * @param appQueryRequest 查询请求
     * @return 查询条件
     */
    @Override
    public QueryWrapper getFeaturedQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        String appName = appQueryRequest.getAppName();

        return QueryWrapper.create()
                .gt("priority", 0)
                .like("appName", appName)
                .orderBy("priority", false);  // 按优先级降序
    }

    /**
     * 获取管理员查询条件（支持根据除时间外的任何字段查询）
     *
     * @param appAdminQueryRequest 管理员查询请求
     * @return 查询条件
     */
    @Override
    public QueryWrapper getAdminQueryWrapper(AppAdminQueryRequest appAdminQueryRequest) {
        if (appAdminQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        Long id = appAdminQueryRequest.getId();
        String appName = appAdminQueryRequest.getAppName();
        String cover = appAdminQueryRequest.getCover();
        String initPrompt = appAdminQueryRequest.getInitPrompt();
        String codeGenType = appAdminQueryRequest.getCodeGenType();
        String deployKey = appAdminQueryRequest.getDeployKey();
        Integer priority = appAdminQueryRequest.getPriority();
        Long userId = appAdminQueryRequest.getUserId();
        String sortField = appAdminQueryRequest.getSortField();
        String sortOrder = appAdminQueryRequest.getSortOrder();

        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .like("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }
}
