package com.toolrecommend.user.controller;

import com.toolrecommend.common.dto.UserLoginDTO;
import com.toolrecommend.common.dto.UserRegisterDTO;
import com.toolrecommend.common.result.Result;
import com.toolrecommend.common.vo.LoginVO;
import com.toolrecommend.common.vo.UserVO;
import com.toolrecommend.user.service.UserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户控制器
 *
 * @author Tool Recommend Team
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserVO> register(@Validated @RequestBody UserRegisterDTO registerDTO) {
        UserVO userVO = userService.register(registerDTO);
        return Result.success("注册成功", userVO);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody UserLoginDTO loginDTO, HttpServletRequest request) {
        String ip = getClientIp(request);
        LoginVO loginVO = userService.login(loginDTO, ip);
        return Result.success("登录成功", loginVO);
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh-token")
    public Result<LoginVO> refreshToken(@RequestParam String refreshToken) {
        LoginVO loginVO = userService.refreshToken(refreshToken);
        return Result.success("刷新成功", loginVO);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser(@RequestHeader("User-Id") Long userId) {
        UserVO userVO = userService.getUserById(userId);
        return Result.success(userVO);
    }

    /**
     * 根据ID查询用户信息
     */
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        UserVO userVO = userService.getUserById(id);
        return Result.success(userVO);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public Result<Void> updateUser(
            @PathVariable Long id,
            @RequestBody UserVO userVO,
            @RequestHeader("User-Id") Long currentUserId) {

        // 只能更新自己的信息
        if (!id.equals(currentUserId)) {
            return Result.error(403, "无权限修改他人信息");
        }

        boolean success = userService.updateUser(id, userVO);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 修改密码
     */
    @PostMapping("/{id}/change-password")
    public Result<Void> changePassword(
            @PathVariable Long id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestHeader("User-Id") Long currentUserId) {

        // 只能修改自己的密码
        if (!id.equals(currentUserId)) {
            return Result.error(403, "无权限修改他人密码");
        }

        boolean success = userService.changePassword(id, oldPassword, newPassword);
        return success ? Result.success("密码修改成功") : Result.error("密码修改失败");
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    public Result<Boolean> checkUsernameExists(@RequestParam String username) {
        boolean exists = userService.checkUsernameExists(username);
        return Result.success(exists);
    }

    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check-email")
    public Result<Boolean> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.checkEmailExists(email);
        return Result.success(exists);
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
