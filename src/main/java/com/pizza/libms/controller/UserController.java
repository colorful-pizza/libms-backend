package com.pizza.libms.controller;

import com.pizza.libms.common.ApiResponse;
import com.pizza.libms.common.PageResult;
import com.pizza.libms.dto.UserDTO;
import com.pizza.libms.dto.UserQuery;
import com.pizza.libms.service.UserService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }

    private record CreateReq(@NotBlank String username, @NotBlank String password,
                             @NotBlank String fullName, @NotBlank String role) {}
    private record UpdateReq(@NotBlank String fullName, @NotBlank String role) {}
    private record UpdatePwdReq(@NotBlank String password) {}

    @GetMapping("/{id}")
    public ApiResponse<UserDTO> get(@PathVariable("id") Long id) {
        UserDTO dto = userService.getById(id);
        if (dto == null) { return ApiResponse.fail(404, "用户不存在"); }
        return ApiResponse.success(dto);
    }

    @GetMapping
    public ApiResponse<PageResult<UserDTO>> page(@RequestParam(value = "username", required = false) String username,
                                                 @RequestParam(value = "role", required = false) String role,
                                                 @RequestParam(value = "page", required = false) Integer page,
                                                 @RequestParam(value = "size", required = false) Integer size) {
        UserQuery q = new UserQuery();
        q.setUsername(username);
        q.setRole(role);
        if (page != null) q.setPage(page);
        if (size != null) q.setSize(size);
        return ApiResponse.success(userService.page(q));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody CreateReq req) {
        Long id = userService.create(req.username(), req.password(), req.fullName(), req.role());
        return ApiResponse.success(id);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable("id") Long id, @RequestBody UpdateReq req) {
        userService.updateBasic(id, req.fullName(), req.role());
        return ApiResponse.success(null);
    }

    /**
     * 修改密码：仅允许用户本人修改自己的密码，无需传 id
     */
    @PutMapping("/password")
    public ApiResponse<Void> updatePassword(@RequestBody UpdatePwdReq req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return ApiResponse.fail(401, "未认证");
        }
        UserDTO dto = userService.getByUsername(auth.getName());
        if (dto == null) { return ApiResponse.fail(404, "用户不存在"); }
        userService.updatePassword(dto.getId(), req.password());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        userService.deleteById(id);
        return ApiResponse.success(null);
    }

        /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    public ApiResponse<UserDTO> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return ApiResponse.fail(401, "未认证");
        }
        UserDTO dto = userService.getByUsername(auth.getName());
        if (dto == null) { return ApiResponse.fail(404, "用户不存在"); }
        return ApiResponse.success(dto);
    }
}
