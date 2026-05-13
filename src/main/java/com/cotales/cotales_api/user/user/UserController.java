package com.cotales.cotales_api.user.user;

import com.cotales.cotales_api.common.ApiController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController extends ApiController {

    private final UserService userService;
}
