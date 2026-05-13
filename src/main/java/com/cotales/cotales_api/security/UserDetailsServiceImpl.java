package com.cotales.cotales_api.security;

import com.cotales.cotales_api.user.account.AccountRepository;
import com.cotales.cotales_api.user.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        com.cotales.cotales_api.user.user.User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        com.cotales.cotales_api.user.account.Account account =
                accountRepository
                        .findById(user.getId())
                        .orElseThrow(() -> new UsernameNotFoundException("Account not found"));

        return User.withUsername(user.getEmail())
                .password(account.getPasswordHash())
                .authorities("ROLE_USER")
                .build();
    }
}
