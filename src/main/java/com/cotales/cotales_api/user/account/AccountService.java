package com.cotales.cotales_api.user.account;

import com.cotales.cotales_api.auth.Provider;
import com.cotales.cotales_api.user.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public void createAccount(User user, Provider provider, String password, String email) {
        accountRepository.save(
                new Account(user, provider, email, passwordEncoder.encode(password)));
    }
}
