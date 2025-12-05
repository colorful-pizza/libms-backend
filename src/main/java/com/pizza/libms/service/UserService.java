package com.pizza.libms.service;

import com.pizza.libms.common.PageResult;
import com.pizza.libms.dto.UserDTO;
import com.pizza.libms.dto.UserQuery;
import com.pizza.libms.entity.User;
import com.pizza.libms.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDTO getById(Long id) {
        User u = userRepository.findById(id);
        if (u == null) { return null; }
        return toDTO(u);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public Long create(String username, String password, String fullName, String role) {
        if (userRepository.findByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(password));
        u.setFullName(fullName);
        u.setRole(role);
        userRepository.insert(u);
        return u.getId();
    }

    public void updateBasic(Long id, String fullName, String role) {
        User u = userRepository.findById(id);
        if (u == null) { throw new IllegalArgumentException("用户不存在"); }
        u.setFullName(fullName);
        u.setRole(role);
        userRepository.updateBasic(u);
    }

    public void updatePassword(Long id, String newPassword) {
        User u = userRepository.findById(id);
        if (u == null) { throw new IllegalArgumentException("用户不存在"); }
        userRepository.updatePassword(id, passwordEncoder.encode(newPassword));
    }
    
    /**
     * 根据用户名获取用户信息（DTO）
     */
    public UserDTO getByUsername(String username) {
        User u = userRepository.findByUsername(username);
        if (u == null) { return null; }
        return toDTO(u);
    }

    public PageResult<UserDTO> page(UserQuery query) {
        int page = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int size = query.getSize() == null || query.getSize() < 1 ? 10 : Math.min(query.getSize(), 100);
        int offset = (page - 1) * size;
        long total = userRepository.count(query.getUsername(), query.getRole());
        List<UserDTO> list = userRepository.list(query.getUsername(), query.getRole(), offset, size)
                .stream().map(this::toDTO).collect(Collectors.toList());
        return new PageResult<>(total, page, size, list);
    }

    private UserDTO toDTO(User u) {
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setFullName(u.getFullName());
        dto.setRole(u.getRole());
        return dto;
    }
}
