package com.toolrecommend.user.service.impl;

import com.toolrecommend.common.dto.UserLoginDTO;
import com.toolrecommend.common.dto.UserRegisterDTO;
import com.toolrecommend.common.entity.User;
import com.toolrecommend.common.exception.BusinessException;
import com.toolrecommend.common.util.JwtUtil;
import com.toolrecommend.common.vo.LoginVO;
import com.toolrecommend.common.vo.UserVO;
import com.toolrecommend.user.mapper.UserMapper;
import com.toolrecommend.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

/**
 * 用户服务实现类
 *
 * @author Tool Recommend Team
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(UserRegisterDTO registerDTO) {
        // 检查用户名是否已存在
        if (checkUsernameExists(registerDTO.getUsername())) {
            throw new BusinessException(1002, "用户名已存在");
        }

        // 检查邮箱是否已存在
        if (checkEmailExists(registerDTO.getEmail())) {
            throw new BusinessException(1002, "邮箱已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getUsername());
        user.setRole("USER");
        user.setStatus(1);

        userMapper.insert(user);

        log.info("用户注册成功: username={}", user.getUsername());

        return convertToUserVO(user);
    }

    @Override
    public LoginVO login(UserLoginDTO loginDTO, String ip) {
        // 根据账号查询用户
        User user = userMapper.selectByAccount(loginDTO.getAccount());
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(1003, "密码错误");
        }

        // 更新最后登录信息
        userMapper.updateLastLogin(user.getId(), ip);

        // 生成Token
        String accessToken = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = JwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        log.info("用户登录成功: username={}, ip={}", user.getUsername(), ip);

        // 返回登录信息
        UserVO userVO = convertToUserVO(user);
        return new LoginVO(accessToken, refreshToken, 604800L, userVO);
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        try {
            // 验证刷新Token
            if (!JwtUtil.validateToken(refreshToken)) {
                throw new BusinessException(1004, "Token已过期");
            }

            // 从Token中获取用户信息
            Long userId = JwtUtil.getUserIdFromToken(refreshToken);
            String username = JwtUtil.getUsernameFromToken(refreshToken);

            // 查询用户
            User user = userMapper.selectById(userId);
            if (user == null || user.getStatus() != 1) {
                throw new BusinessException(1001, "用户不存在或已被禁用");
            }

            // 生成新的Token
            String newAccessToken = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
            String newRefreshToken = JwtUtil.generateRefreshToken(user.getId(), user.getUsername());

            UserVO userVO = convertToUserVO(user);
            return new LoginVO(newAccessToken, newRefreshToken, 604800L, userVO);

        } catch (Exception e) {
            throw new BusinessException(1005, "Token无效");
        }
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }
        return convertToUserVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(Long id, UserVO userVO) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }

        // 只允许更新部分字段
        if (userVO.getNickname() != null) {
            user.setNickname(userVO.getNickname());
        }
        if (userVO.getAvatarUrl() != null) {
            user.setAvatarUrl(userVO.getAvatarUrl());
        }
        if (userVO.getBio() != null) {
            user.setBio(userVO.getBio());
        }

        int result = userMapper.updateById(user);
        log.info("用户信息更新: userId={}", id);

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BusinessException(1003, "原密码错误");
        }

        // 更新密码
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        int result = userMapper.updateById(user);

        log.info("用户密码修改成功: userId={}", id);

        return result > 0;
    }

    @Override
    public boolean checkUsernameExists(String username) {
        User user = userMapper.selectByUsername(username);
        return user != null;
    }

    @Override
    public boolean checkEmailExists(String email) {
        User user = userMapper.selectByEmail(email);
        return user != null;
    }

    /**
     * 转换为UserVO
     */
    private UserVO convertToUserVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        // 不返回密码
        return vo;
    }
}
