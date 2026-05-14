package com.cotales.cotales_api.user.user;

import com.cotales.cotales_api.auth.Provider;
import com.cotales.cotales_api.common.PageResponse;
import com.cotales.cotales_api.common.exception.ConflictException;
import com.cotales.cotales_api.user.account.AccountService;
import com.cotales.cotales_api.user.profile.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final AccountService accountService;
    private final ProfileService profileService;

    @Transactional
    public UserResponse register(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already in use");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already in use");
        }

        var user = userRepository.save(new User(request.username(), request.email()));

        profileService.createProfile(user);
        accountService.createAccount(user, Provider.PASSWORD, request.email(), request.password());

        return new UserResponse(
                user.getId(), user.getUsername(), user.getEmail(), user.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        return PageResponse.of(
                userRepository
                        .findAllByDeletedAtIsNull(pageable)
                        .map(
                                user ->
                                        new UserResponse(
                                                user.getId(),
                                                user.getUsername(),
                                                user.getEmail(),
                                                user.getCreatedAt())));
    }
}
