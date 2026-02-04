package com.company.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.company.ai.model.enums.CodeGenTypeEnum;
import com.company.constant.AppConstant;
import com.company.core.AiCodeGeneratorFacade;
import com.company.exception.BusinessException;
import com.company.exception.ErrorCode;
import com.company.exception.ThrowUtils;
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
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;


import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;
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
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    /**
     * 应用聊天生成代码（流式SSE）
     *
     * @param message
     * @param appId
     * @param loginUser
     * @return
     */
    @Override
    public Flux<String> chatToGenCode(String message, Long appId, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        //查询应用信息
        App app = new App();
        app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        //验证用户权限
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问");
        }
        //获取生成代码类型
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        // 如果未设置生成类型，使用默认值 MULTI_FILE
        if (codeGenTypeEnum == null) {
            codeGenTypeEnum = CodeGenTypeEnum.MULTI_FILE;
        }
        //调用生成代码接口
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        //参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.PARAMS_ERROR, "用户未登录");
        //查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        //验证用户是否有权限部署应用（仅本人）
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作");
        }
        //检查是否有deployKey，没有就生成6位的deployKey（大小写字母+数字）
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        //获取代码生成类型
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + app.getId();
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;

        //检查代码目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists()||!sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "代码目录不存在");
        }
        //复制代码到部署目录中
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try{
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        }catch (Exception e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码复制失败");
        }

        //更新deployKey和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.SYSTEM_ERROR, "更新应用失败");
        //返回可访问的URL
        return String.format("%s/%s/",AppConstant.CODE_DEPLOY_HOST,deployKey);
    }

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
