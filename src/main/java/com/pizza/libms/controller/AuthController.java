package com.pizza.libms.controller;

import com.pizza.libms.common.ApiResponse;
import com.pizza.libms.service.AuthService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/")
@Validated
public class AuthController {

    private record LoginReq(@NotBlank String username, @NotBlank String password) {}
    private record RegisterReq(@NotBlank String username, @NotBlank String password,
                               @NotBlank String fullName, String role) {}

    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("login")
    public ApiResponse<Map<String, Object>> login(@RequestBody LoginReq req) {
        String token = authService.login(req.username(), req.password());
        return ApiResponse.success(Map.of("token", token));
    }

    @PostMapping("register")
    public ApiResponse<Void> register(@RequestBody RegisterReq req) {
        authService.register(req.username(), req.password(), req.fullName(), req.role());
        return ApiResponse.success(null);
    }
}
