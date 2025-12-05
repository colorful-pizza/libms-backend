package com.pizza.libms.service;

import com.pizza.libms.entity.User;
import com.pizza.libms.repository.UserRepository;
import com.pizza.libms.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String login(String username, String rawPassword) {
        User user = userRepository.findByUsername(username);
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());
        claims.put("name", user.getFullName());
        return jwtUtil.generateToken(user.getUsername(), claims);
    }

    public void register(String username, String rawPassword, String fullName, String role) {
        if (userRepository.findByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setRole(role == null ? "USER" : role);
        userRepository.insert(user);
    }
}
