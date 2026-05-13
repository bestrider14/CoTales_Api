package com.cotales.cotales_api.user.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cotales.cotales_api.user.account.Account;
import com.cotales.cotales_api.user.account.AccountRepository;
import com.cotales.cotales_api.user.profile.Profile;
import com.cotales.cotales_api.user.profile.ProfileRepository;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    private static final CreateUserRequest VALID_REQUEST =
            new CreateUserRequest("luka", "luka@mail.com", "motdepasse123");

    @Test
    void register_withValidRequest_returnsUserResponse() {
        when(userRepository.existsByEmail(VALID_REQUEST.email())).thenReturn(false);
        when(userRepository.existsByUsername(VALID_REQUEST.username())).thenReturn(false);
        when(passwordEncoder.encode(VALID_REQUEST.password())).thenReturn("hashed");

        User savedUser = new User(VALID_REQUEST.username(), VALID_REQUEST.email());
        savedUser.setId(1L);
        savedUser.setCreatedAt(OffsetDateTime.now());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.register(VALID_REQUEST);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("luka");
        assertThat(response.email()).isEqualTo("luka@mail.com");
        assertThat(response.createdAt()).isNotNull();
    }

    @Test
    void register_createsUserProfileAndAccount() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");

        User savedUser = new User(VALID_REQUEST.username(), VALID_REQUEST.email());
        savedUser.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        userService.register(VALID_REQUEST);

        verify(userRepository).save(any(User.class));
        verify(profileRepository).save(any(Profile.class));
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void register_hashesPasswordBeforeSaving() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(passwordEncoder.encode(VALID_REQUEST.password())).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(new User("luka", "luka@mail.com"));

        userService.register(VALID_REQUEST);

        verify(passwordEncoder).encode(VALID_REQUEST.password());
    }

    @Test
    void register_whenEmailAlreadyInUse_throwsConflict() {
        when(userRepository.existsByEmail(VALID_REQUEST.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(VALID_REQUEST))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(
                        ex ->
                                assertThat(((ResponseStatusException) ex).getStatusCode())
                                        .isEqualTo(HttpStatus.CONFLICT));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_whenUsernameAlreadyInUse_throwsConflict() {
        when(userRepository.existsByEmail(VALID_REQUEST.email())).thenReturn(false);
        when(userRepository.existsByUsername(VALID_REQUEST.username())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(VALID_REQUEST))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(
                        ex ->
                                assertThat(((ResponseStatusException) ex).getStatusCode())
                                        .isEqualTo(HttpStatus.CONFLICT));

        verify(userRepository, never()).save(any());
    }
}
