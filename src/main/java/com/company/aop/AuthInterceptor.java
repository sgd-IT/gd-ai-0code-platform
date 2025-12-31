package com.company.aop;

import com.company.annotation.AuthCheck;
import com.company.exception.BusinessException;
import com.company.exception.ErrorCode;
import com.company.model.entity.User;
import com.company.model.enums.UserRoleEnum;
import com.company.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 权限校验切面
 * 通过AOP拦截带有@AuthCheck注解的方法，进行用户权限验证
 * 核心机制：利用Spring的RequestContextHolder从当前线程上下文中获取请求信息
 */
@Aspect  // 标记为AOP切面
@Component  // 注册为Spring Bean
public class AuthInterceptor {
    @Resource  // 依赖注入UserService
    UserService userService;

    /**
     * 环绕通知：拦截所有带有@AuthCheck注解的方法
     *
     * 核心机制说明：
     * 当HTTP请求进入Spring应用时，DispatcherServlet会在请求开始处理前执行：
     *   RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));
     * 这行代码将请求和响应对象包装后存入当前线程的ThreadLocal变量中。
     *
     * ThreadLocal是一种线程局部变量存储机制，每个线程都有自己独立的变量副本。
     * 在Web应用中，每个HTTP请求由独立的线程处理，因此通过ThreadLocal可以实现：
     *   1. 在同一请求处理链路中任意位置访问请求信息
     *   2. 不同请求之间的数据完全隔离，不会互相干扰
     *   3. 无需通过方法参数显式传递请求对象，降低代码耦合度
     *
     * 本拦截器正是利用这一机制，在不修改目标方法签名的情况下获取HTTP请求信息
     *
     * @param joinPoint 连接点，代表被拦截的方法
     * @param authCheck 注入的AuthCheck注解实例
     * @return 方法执行结果
     * @throws Throwable 任何异常
     */
    @Around("@annotation(authCheck)")  // 定义切入点：所有使用@AuthCheck注解的方法
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 1. 从注解中获取必需的角色标识
        String mustRole = authCheck.mustRole();

        // 2. 从当前线程的ThreadLocal中获取请求属性
        // 关键机制：Spring在请求开始时已将请求信息存入当前线程的ThreadLocal
        // RequestContextHolder内部使用InheritableThreadLocal<RequestAttributes>存储请求上下文
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();

        // 3. 将通用请求属性转换为Servlet特定的请求属性
        // ServletRequestAttributes是Spring对HttpServletRequest的封装
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 4. 通过UserService从请求中解析当前登录用户
        // 通常实现：从session、token或cookie中获取用户身份信息
        User loginUser = userService.getLoginUser(request);

        // 5. 将必需角色字符串转换为枚举类型
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);

        // 6. 如果没有指定必需角色（或角色无效），直接放行
        if(mustRoleEnum == null) {
            return joinPoint.proceed();  // 执行原始方法
        }

        // 7. 将当前用户的角色字符串转换为枚举类型
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());

        // 8. 验证用户角色是否有效
        if(userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户角色不存在");
        }

        // 9. 【注意：此处逻辑有误】权限校验
        // 错误逻辑：当用户不是管理员且用户角色等于必需角色时，反而拒绝访问
        // 正确逻辑应为：
        //   if (!UserRoleEnum.ADMIN.equals(userRoleEnum) && !userRoleEnum.equals(mustRoleEnum)) {
        //       throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户无权限");
        //   }
        // 或更清晰的写法：
        //   if (UserRoleEnum.ADMIN.equals(userRoleEnum)) {
        //       return joinPoint.proceed(); // 管理员放行
        //   }
        //   if (!userRoleEnum.equals(mustRoleEnum)) {
        //       throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户无权限");
        //   }
        if (!userRoleEnum.ADMIN.equals(userRoleEnum) && !userRoleEnum.equals(mustRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户无权限");
        }

        // 10. 权限校验通过，执行原始方法
        return joinPoint.proceed();
    }
}