package com.company.controller;

import cn.hutool.core.bean.BeanUtil;
import com.company.annotation.AuthCheck;
import com.company.common.BaseResponse;
import com.company.common.ResultUtils;
import com.company.constant.UserConstant;
import com.company.exception.ErrorCode;
import com.company.exception.ThrowUtils;
import com.company.model.dto.AppAddRequest;
import com.company.model.dto.AppAdminQueryRequest;
import com.company.model.dto.AppAdminUpdateRequest;
import com.company.model.dto.AppQueryRequest;
import com.company.model.dto.AppUpdateRequest;
import com.company.model.entity.App;
import com.company.model.entity.User;
import com.company.model.vo.AppVO;
import com.company.service.AppService;
import com.company.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 应用 控制层。
 *
 * @author gd
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    // region 用户接口

    /**
     * 【用户】创建应用（须填写 initPrompt）
     *
     * @param appAddRequest 创建应用请求
     * @param request       HTTP请求
     * @return 创建的应用id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR, "参数为空");

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 构建应用对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());

        // 校验数据
        appService.validApp(app, true);

        // 保存应用
        boolean result = appService.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建应用失败");

        return ResultUtils.success(app.getId());
    }

    /**
     * 【用户】根据 id 修改自己的应用（目前只支持修改应用名称）
     *
     * @param appUpdateRequest 更新应用请求
     * @param request          HTTP请求
     * @return 是否成功
     */
    @PutMapping("/update")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR, "参数为空");

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 判断应用是否存在
        Long id = appUpdateRequest.getId();
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 校验是否是自己的应用
        ThrowUtils.throwIf(!oldApp.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限修改此应用");

        // 构建更新对象（仅支持修改应用名称）
        App app = new App();
        app.setId(id);
        app.setAppName(appUpdateRequest.getAppName());

        // 校验数据
        appService.validApp(app, false);

        // 更新应用
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新应用失败");

        return ResultUtils.success(true);
    }

    /**
     * 【用户】根据 id 删除自己的应用
     *
     * @param id      应用id
     * @param request HTTP请求
     * @return 是否成功
     */
    @DeleteMapping("/delete/{id}")
    public BaseResponse<Boolean> deleteApp(@PathVariable Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "参数为空");

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 判断应用是否存在
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 校验是否是自己的应用
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限删除此应用");

        // 删除应用
        boolean result = appService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除应用失败");

        return ResultUtils.success(true);
    }

    /**
     * 【用户】根据 id 查看应用详情
     *
     * @param id      应用id
     * @param request HTTP请求
     * @return 应用详情
     */
    @GetMapping("/get/vo/{id}")
    public BaseResponse<AppVO> getAppVOById(@PathVariable Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "参数为空");

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 获取应用
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 校验是否是自己的应用
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限查看此应用");

        return ResultUtils.success(appService.getAppVO(app));
    }

    /**
     * 【用户】分页查询自己的应用列表（支持根据名称查询，每页最多 20 个）
     *
     * @param appQueryRequest 查询请求
     * @param request         HTTP请求
     * @return 应用列表
     */
    @GetMapping("/list/page/vo")
    public BaseResponse<Page<AppVO>> listMyAppVOByPage(AppQueryRequest appQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR, "参数为空");

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 限制每页最多 20 个
        int pageNum = appQueryRequest.getPageNum();
        int pageSize = Math.min(appQueryRequest.getPageSize(), 20);

        // 分页查询
        Page<App> page = appService.page(
                Page.of(pageNum, pageSize),
                appService.getQueryWrapper(appQueryRequest, loginUser.getId())
        );

        // 转换为VO
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, page.getTotalRow());
        List<AppVO> appVOList = appService.getListAppVO(page.getRecords());
        appVOPage.setRecords(appVOList);

        return ResultUtils.success(appVOPage);
    }

    /**
     * 【用户】分页查询精选的应用列表（支持根据名称查询，每页最多 20 个）
     *
     * @param appQueryRequest 查询请求
     * @return 精选应用列表
     */
    @GetMapping("/list/page/vo/featured")
    public BaseResponse<Page<AppVO>> listFeaturedAppVOByPage(AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR, "参数为空");

        // 限制每页最多 20 个
        int pageNum = appQueryRequest.getPageNum();
        int pageSize = Math.min(appQueryRequest.getPageSize(), 20);

        // 分页查询精选应用
        Page<App> page = appService.page(
                Page.of(pageNum, pageSize),
                appService.getFeaturedQueryWrapper(appQueryRequest)
        );

        // 转换为VO
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, page.getTotalRow());
        List<AppVO> appVOList = appService.getListAppVO(page.getRecords());
        appVOPage.setRecords(appVOList);

        return ResultUtils.success(appVOPage);
    }

    // endregion

    // region 管理员接口

    /**
     * 【管理员】根据 id 删除任意应用
     *
     * @param id 应用id
     * @return 是否成功
     */
    @DeleteMapping("/admin/delete/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> adminDeleteApp(@PathVariable Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "参数为空");

        // 判断应用是否存在
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 删除应用
        boolean result = appService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除应用失败");

        return ResultUtils.success(true);
    }

    /**
     * 【管理员】根据 id 更新任意应用（支持更新应用名称、应用封面、优先级）
     *
     * @param appAdminUpdateRequest 管理员更新请求
     * @return 是否成功
     */
    @PutMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> adminUpdateApp(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        ThrowUtils.throwIf(appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR, "参数为空");

        // 判断应用是否存在
        Long id = appAdminUpdateRequest.getId();
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 构建更新对象
        App app = new App();
        app.setId(id);
        app.setAppName(appAdminUpdateRequest.getAppName());
        app.setCover(appAdminUpdateRequest.getCover());
        app.setPriority(appAdminUpdateRequest.getPriority());

        // 校验数据
        appService.validApp(app, false);

        // 更新应用
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新应用失败");

        return ResultUtils.success(true);
    }

    /**
     * 【管理员】分页查询应用列表（支持根据除时间外的任何字段查询，每页数量不限）
     *
     * @param appAdminQueryRequest 管理员查询请求
     * @return 应用列表
     */
    @GetMapping("/admin/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<App>> adminListAppByPage(AppAdminQueryRequest appAdminQueryRequest) {
        ThrowUtils.throwIf(appAdminQueryRequest == null, ErrorCode.PARAMS_ERROR, "参数为空");

        int pageNum = appAdminQueryRequest.getPageNum();
        int pageSize = appAdminQueryRequest.getPageSize();

        // 分页查询
        Page<App> page = appService.page(
                Page.of(pageNum, pageSize),
                appService.getAdminQueryWrapper(appAdminQueryRequest)
        );

        return ResultUtils.success(page);
    }

    /**
     * 【管理员】根据 id 查看应用详情
     *
     * @param id 应用id
     * @return 应用详情（包含完整信息）
     */
    @GetMapping("/admin/get/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<App> adminGetAppById(@PathVariable Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "参数为空");

        // 获取应用
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        return ResultUtils.success(app);
    }

    // endregion
}
