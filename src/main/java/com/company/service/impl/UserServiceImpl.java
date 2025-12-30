package com.company.service.impl;

import cn.hutool.core.bean.BeanUtil;
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
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.kafka.SslBundleSslEngineFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import static com.company.constant.UserConstant.USER_LOGIN_STATE;

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
     * 获取脱敏后的用户信息
     *
     * @param user
     * @return
     */
    @Override
    public LoginUserVO getUserLoginVO(User  user) {
        //校验
        if(user==null){
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user,loginUserVO);
        return loginUserVO;
    }

    /**
     * 用户登录
     *
     * @param userAccount
     * @param password
     * @return 脱敏后的用户信息
     */
    public LoginUserVO userLogin(String userAccount, String password, HttpServletRequest request) {
        //校验
        if (StrUtil.hasBlank(userAccount, password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (password.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        //查看用户是否存在
        String encryptPassword = getEncryptPassword(password);

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("password",encryptPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        if(user==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        //记录用户状态到session
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        //返回脱敏后的用户信息
        return this.getUserLoginVO(user);
    }

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) attribute;
        if(currentUser ==null || currentUser.getId()==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"未登录");
        }
        Long id = currentUser.getId();
        currentUser = this.getById(id);
        if(currentUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"用户不存在");
        }

        return currentUser;
    }

    /**
     * 用户登出
     * @param request
     * @return
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
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
