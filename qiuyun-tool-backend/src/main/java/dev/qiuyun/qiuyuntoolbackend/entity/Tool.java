package dev.qiuyun.qiuyuntoolbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 工具实体
 */
@Entity
@Table(name = "tools")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "category_id", insertable = false, updatable = false)
    private Long categoryId;

    @Column(length = 50)
    private String icon;

    @Column(name = "is_vip")
    private Boolean isVip;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "visits_count")
    private Long visitsCount;

    @Column(name = "rating")
    private BigDecimal rating;

    @Column(name = "review_count")
    private Integer reviewCount;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tool_tags",
            joinColumns = @JoinColumn(name = "tool_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    @EqualsAndHashCode.Exclude
    private Set<Tag> tags = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
