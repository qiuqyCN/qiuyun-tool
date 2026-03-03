package dev.qiuyun.qiuyuntoolbackend.service;

import dev.qiuyun.qiuyuntoolbackend.payload.request.LoginRequest;
import dev.qiuyun.qiuyuntoolbackend.payload.request.RegisterRequest;
import dev.qiuyun.qiuyuntoolbackend.payload.response.LoginResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.MessageResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.UserResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户注册
     */
    MessageResponse register(RegisterRequest request);

    /**
     * 刷新令牌
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * 退出登录
     */
    MessageResponse logout(Long userId);

    /**
     * 获取当前用户信息
     */
    UserResponse getCurrentUser(Long userId);
}
