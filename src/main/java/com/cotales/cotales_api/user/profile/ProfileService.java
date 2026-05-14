package com.cotales.cotales_api.user.profile;

import com.cotales.cotales_api.user.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    public void createProfile(User user) {
        profileRepository.save(new Profile(user));
    }
}
