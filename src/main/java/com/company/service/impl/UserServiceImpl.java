package com.company.service.impl;

import cn.hutool.core.util.StrUtil;
import com.company.exception.BusinessException;
import com.company.exception.ErrorCode;
import com.company.model.enums.UserRoleEnum;
import com.company.model.vo.LoginUserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.company.model.entity.User;
import com.company.mapper.UserMapper;
import com.company.service.UserService;
import org.springframework.boot.autoconfigure.kafka.SslBundleSslEngineFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 用户 服务层实现。
 *
 * @author gd
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 用户注册
     *
     * @param userAccount   账号
     * @param password      密码
     * @param checkPassword 确认密码
     * @return
     */
    @Override
    public long userRegister(String userAccount, String password, String checkPassword) {
        //校验
        if (StrUtil.hasBlank(userAccount, password, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (password.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!password.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        //查询账号是否重复
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        //加密
        String encryptPassword = getEncryptPassword(password);
        //插入数据
        User user = User.builder()
                .userAccount(userAccount)
                .userPassword(encryptPassword)
                .userName("张三")
                .userRole(UserRoleEnum.USER.getValue())
                .build();
        boolean saveRusult = this.save(user);
        if (!saveRusult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param userAccount   账号
     * @param password      密码
     * @return
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String password) {
        return null;
    }

    /**
 * 获取加密密码
 * @param password
 * @return
 */
    public String getEncryptPassword(String password) {
        //盐值 混淆密码
        final String salt = "sgd";
        return DigestUtils.md5DigestAsHex((salt + password).getBytes());
    }




}
