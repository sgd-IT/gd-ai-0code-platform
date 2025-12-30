package com.company.service;

import com.company.model.vo.LoginUserVO;
import com.mybatisflex.core.service.IService;
import com.company.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户 服务层。
 *
 * @author gd
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount
     * @param password
     * @param checkPassword
     * @return
     */
    long userRegister(String userAccount,String password,String checkPassword);


    /**
     * 获取脱敏的已登录用户信息
     * @param user
     * @return
     */
    LoginUserVO getUserLoginVO(User user);

    /**
     * 用户登录
     *
     * @param userAccount
     * @param password
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String password,HttpServletRequest request);

    /**
     * 获取当前登录用户
     * @param request
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     */
    boolean userLogout(HttpServletRequest request);
}
