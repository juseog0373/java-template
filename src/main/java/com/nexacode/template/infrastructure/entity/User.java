package com.nexacode.template.infrastructure.entity;

import com.nexacode.template.common.entity.BaseEntity;
import com.nexacode.template.infrastructure.domain.Provider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() where id = ?")
@Table(name = "users")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    @Comment("고유 id")
    private Long id;

    @Column(name = "login_id")
    @Comment("로그인 id")
    private String loginId;

    @Column(name = "email")
    @Comment("email")
    private String email;

    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    @Comment("소셜 로그인 제공업체 ex) GOOGLE: 구글, APPLE: 애플, NAVER: 네이버, KAKAO: 카카오")
    private Provider provider;

    @Column(name = "name")
    @Comment("이름")
    private String name;

    @Column(name = "password")
    @Comment("비밀번호")
    private String password;
}
