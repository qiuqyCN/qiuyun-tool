package dev.qiuyun.qiuyuntoolbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户实体
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column(length = 500)
    private String avatar;

    @Column(length = 20)
    private String phone;

    @Column(name = "is_vip")
    @Builder.Default
    private Boolean isVip = false;

    @Column(name = "vip_expire_date")
    private LocalDateTime vipExpireDate;

    @Column
    @Builder.Default
    private Integer status = 1; // 0-禁用，1-正常

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Builder.Default
    private Set<UserRole> roles = new HashSet<>();

    /**
     * 添加角色
     */
    public void addRole(String roleName) {
        UserRole role = UserRole.builder()
                .user(this)
                .role(roleName)
                .build();
        this.roles.add(role);
    }

    /**
     * 检查用户是否有效
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }

    /**
     * 检查是否是VIP（未过期）
     */
    public boolean isValidVip() {
        if (!Boolean.TRUE.equals(isVip)) {
            return false;
        }
        if (vipExpireDate == null) {
            return true;
        }
        return vipExpireDate.isAfter(LocalDateTime.now());
    }
}
