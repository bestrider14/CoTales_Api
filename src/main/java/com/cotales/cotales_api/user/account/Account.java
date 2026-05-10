package com.cotales.cotales_api.user.account;

import com.cotales.cotales_api.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "Accounts")
public class Account {
  @Id
  @Column(name = "user_id")
  private Long id;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "provider")
  private String provider;

  @Column(name = "provider_user_id")
  private String providerUserId;

  @Column(name = "password_hash")
  private String passwordHash;

  @Column(name = "password_updated_at")
  private OffsetDateTime passwordUpdatedAt;

  @Column(name = "created_at", insertable = false, updatable = false)
  @CreationTimestamp
  private OffsetDateTime createdAt;

  public Account(User user, String provider, String providerUserId, String passwordHash) {
    this.user = user;
    this.provider = provider;
    this.providerUserId = providerUserId;
    this.passwordHash = passwordHash;
  }
}