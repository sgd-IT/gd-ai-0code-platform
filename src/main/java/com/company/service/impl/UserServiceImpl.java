package com.company.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.company.model.entity.User;
import com.company.mapper.UserMapper;
import com.company.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户 服务层实现。
 *
 * @author gd
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

}
