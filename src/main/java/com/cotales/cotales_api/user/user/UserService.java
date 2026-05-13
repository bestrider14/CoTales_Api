package com.cotales.cotales_api.user.user;

import com.cotales.cotales_api.user.account.Account;
import com.cotales.cotales_api.user.account.AccountRepository;
import com.cotales.cotales_api.user.profile.Profile;
import com.cotales.cotales_api.user.profile.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already in use");
        }

        User user = userRepository.save(new User(request.username(), request.email()));

        profileRepository.save(new Profile(user));
        accountRepository.save(
                new Account(
                        user,
                        "PASSWORD",
                        request.email(),
                        passwordEncoder.encode(request.password())));

        return new UserResponse(
                user.getId(), user.getUsername(), user.getEmail(), user.getCreatedAt());
    }
}
