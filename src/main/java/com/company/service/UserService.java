package com.company.service;

import com.company.model.vo.LoginUserVO;
import com.mybatisflex.core.service.IService;
import com.company.model.entity.User;

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
     * 用户登录
     * @param userAccount
     * @param password
     * @return
     */
    LoginUserVO userLogin(String userAccount,String password);
}
