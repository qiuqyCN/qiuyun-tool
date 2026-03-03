package dev.qiuyun.qiuyuntoolbackend.service.impl;

import dev.qiuyun.qiuyuntoolbackend.entity.User;
import dev.qiuyun.qiuyuntoolbackend.entity.UserRole;
import dev.qiuyun.qiuyuntoolbackend.payload.request.LoginRequest;
import dev.qiuyun.qiuyuntoolbackend.payload.request.RegisterRequest;
import dev.qiuyun.qiuyuntoolbackend.payload.response.LoginResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.MessageResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.UserResponse;
import dev.qiuyun.qiuyuntoolbackend.repository.UserRepository;
import dev.qiuyun.qiuyuntoolbackend.security.JwtUtil;
import dev.qiuyun.qiuyuntoolbackend.security.UserDetailsImpl;
import dev.qiuyun.qiuyuntoolbackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    // private static final String BLACKLIST_PREFIX = "token:blacklist:";

    @Override
    public LoginResponse login(LoginRequest request) {
        // 认证用户
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // 生成令牌
        String accessToken = jwtUtil.generateAccessToken(userDetails.getId(), userDetails.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(userDetails.getId(), userDetails.getUsername());

        // 存储刷新令牌到 Redis
        long refreshTokenExpiration = jwtUtil.getRefreshTokenExpiration();
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userDetails.getId(),
                refreshToken,
                refreshTokenExpiration,
                TimeUnit.MILLISECONDS
        );

        // 更新登录信息
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        // 构建响应
        List<String> roles = userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
                .user(LoginResponse.UserInfo.builder()
                        .id(userDetails.getId())
                        .username(userDetails.getUsername())
                        .nickname(userDetails.getNickname())
                        .email(userDetails.getEmail())
                        .avatar(userDetails.getAvatar())
                        .isVip(userDetails.getIsVip())
                        .roles(roles)
                        .build())
                .build();
    }

    @Override
    @Transactional
    public MessageResponse register(RegisterRequest request) {
        // 检查用户名
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已被使用");
        }

        // 检查邮箱
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 检查密码
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        // 创建用户
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getUsername())
                .isVip(false)
                .status(1)
                .build();

        // 添加默认角色
        user.addRole("USER");

        userRepository.save(user);

        return MessageResponse.success("注册成功");
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        // 验证刷新令牌
        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("无效的刷新令牌");
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);

        // 检查 Redis 中的刷新令牌是否匹配
        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BadCredentialsException("刷新令牌已过期或无效");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 生成新的令牌
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 更新 Redis 中的刷新令牌
        long refreshTokenExpiration = jwtUtil.getRefreshTokenExpiration();
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                newRefreshToken,
                refreshTokenExpiration,
                TimeUnit.MILLISECONDS
        );

        List<String> roles = user.getRoles().stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .email(user.getEmail())
                        .avatar(user.getAvatar())
                        .isVip(user.getIsVip())
                        .roles(roles)
                        .build())
                .build();
    }

    @Override
    public MessageResponse logout(Long userId) {
        // 删除刷新令牌
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);

        // 将当前访问令牌加入黑名单（可选，如果需要立即失效）
        // 这里可以获取当前请求的令牌并加入黑名单

        SecurityContextHolder.clearContext();

        return MessageResponse.success("退出成功");
    }

    @Override
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<String> roles = user.getRoles().stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .isVip(user.getIsVip())
                .vipExpireDate(user.getVipExpireDate())
                .status(user.getStatus())
                .roles(roles)
                .lastLoginTime(user.getLastLoginTime())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
