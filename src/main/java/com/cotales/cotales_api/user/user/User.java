package com.cotales.cotales_api.user.user;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "created_at", insertable = false, updatable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "token_balance")
    private Integer tokenBalance;

    @Column(name = "last_login")
    private OffsetDateTime lastLogin;

    @Column(name = "email_updated_at")
    private OffsetDateTime emailUpdatedAt;

    @Column(name = "is_hidden")
    private Boolean isHidden;

    @Column(name = "banned_at")
    private OffsetDateTime bannedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }
}
