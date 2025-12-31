package com.company.controller;

import cn.hutool.core.bean.BeanUtil;
import com.company.annotation.AuthCheck;
import com.company.common.BaseResponse;
import com.company.common.ResultUtils;
import com.company.constant.UserConstant;
import com.company.exception.ErrorCode;
import com.company.exception.ThrowUtils;
import com.company.model.dto.*;
import com.company.model.vo.LoginUserVO;
import com.company.model.vo.UserVO;
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
import org.springframework.beans.factory.annotation.Autowired;
import com.company.model.entity.User;
import com.company.service.UserService;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 用户 控制层。
 *
 * @author gd
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 创建的用户id
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest==null, ErrorCode.PARAMS_ERROR, "参数为空");
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount, userPassword, checkPassword);

        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest==null, ErrorCode.PARAMS_ERROR, "参数为空");
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getUserLoginVO(user));
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request==null, ErrorCode.OPERATION_ERROR, "未登录");
        return ResultUtils.success(userService.userLogout(request));
    }


    /**
     * 添加用户。
     *
     * @param userAddRequest 用户
     * @return 添加的用户
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest==null, ErrorCode.PARAMS_ERROR, "参数为空");
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        String encryptPassword = userService.getEncryptPassword(UserConstant.DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "用户添加失败");
        return ResultUtils.success(user);
    }

    /**
     * 根据id获取用户（管理员）
     * @param id
     * @return
     */
    @GetMapping("/get/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(@PathVariable Long id) {
        ThrowUtils.throwIf(id==null, ErrorCode.PARAMS_ERROR, "参数为空");
        User user = userService.getById(id);
        ThrowUtils.throwIf(user==null, ErrorCode.OPERATION_ERROR, "用户不存在");
        return ResultUtils.success(user);
    }

    /**
     * 根据id获取包装类
     */
    @GetMapping("/get/vo/{id}")
    public BaseResponse<UserVO> getUserVOById(@PathVariable Long id) {
        ThrowUtils.throwIf(id==null, ErrorCode.PARAMS_ERROR, "参数为空");
        BaseResponse<User> response=getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 根据id删除用户
     */
    @DeleteMapping("/delete/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@PathVariable Long id) {
        ThrowUtils.throwIf(id==null, ErrorCode.PARAMS_ERROR, "参数为空");
        User user = userService.getById(id);
        ThrowUtils.throwIf(user==null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        boolean result = userService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "用户删除失败");
        return ResultUtils.success(true);
    }

    /**
     * 更新用户
     */
    @PutMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest==null, ErrorCode.PARAMS_ERROR, "参数为空");
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "用户更新失败");
        return ResultUtils.success(true);
    }

    /**
     * 分页获取用户列表（脱敏信息）
     */
    @GetMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest==null, ErrorCode.PARAMS_ERROR, "参数为空");
        long pageNum=userQueryRequest.getPageNum();
        long pageSize=userQueryRequest.getPageSize();
        Page<User> page=userService.page(Page.of(pageNum, pageSize),userService.getQueryWrapper(userQueryRequest));
        //数据脱敏
        ThrowUtils.throwIf(page==null, ErrorCode.OPERATION_ERROR, "用户列表获取失败");
        Page<UserVO> userVOPage=new Page<>(pageNum, pageSize, page.getTotalRow());
        List<UserVO> userVOList = userService.getListUserVO(page.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }
}
