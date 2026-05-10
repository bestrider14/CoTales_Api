package com.cotales.cotales_api.user.profile;

import com.cotales.cotales_api.user.User;
import com.cotales.cotales_api.user.profile.Address.Country;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "Profiles")
public class Profile {
  @Id
  @Column(name = "user_id")
  private Long id;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "first_name")
  private String firstName;

  @Size(max = 100)
  @Column(name = "last_name")
  private String lastName;

  @Column(name = "bio")
  private String bio;

  @Column(name = "birth_date")
  private LocalDate birthDate;

  @Column(name = "gender")
  private String gender;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "country_id")
  private Country country;

  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  public Profile(User user) {
    this.user = user;
  }
}