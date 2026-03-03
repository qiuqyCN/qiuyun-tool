package dev.qiuyun.qiuyuntoolbackend.controller;

import dev.qiuyun.qiuyuntoolbackend.payload.request.LoginRequest;
import dev.qiuyun.qiuyuntoolbackend.payload.request.RegisterRequest;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ApiResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.LoginResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.MessageResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.UserResponse;
import dev.qiuyun.qiuyuntoolbackend.security.UserDetailsImpl;
import dev.qiuyun.qiuyuntoolbackend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        MessageResponse response = authService.register(request);
        return ApiResponse.success(response);
    }

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        String refreshToken = authHeader.replace("Bearer ", "");
        LoginResponse response = authService.refreshToken(refreshToken);
        return ApiResponse.success(response);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ApiResponse<MessageResponse> logout(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        MessageResponse response = authService.logout(userDetails.getId());
        return ApiResponse.success(response);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserResponse response = authService.getCurrentUser(userDetails.getId());
        return ApiResponse.success(response);
    }
}
