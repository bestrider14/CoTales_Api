package com.cotales.cotales_api.user.account;

import com.cotales.cotales_api.common.ApiController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/accounts")
public class AccountController extends ApiController {

    private final AccountService accountService;
}
