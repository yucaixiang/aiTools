package com.toolrecommend.user.service;

import com.toolrecommend.common.dto.UserLoginDTO;
import com.toolrecommend.common.dto.UserRegisterDTO;
import com.toolrecommend.common.vo.LoginVO;
import com.toolrecommend.common.vo.UserVO;

/**
 * 用户服务接口
 *
 * @author Tool Recommend Team
 */
public interface UserService {

    /**
     * 用户注册
     */
    UserVO register(UserRegisterDTO registerDTO);

    /**
     * 用户登录
     */
    LoginVO login(UserLoginDTO loginDTO, String ip);

    /**
     * 根据Token刷新Token
     */
    LoginVO refreshToken(String refreshToken);

    /**
     * 根据ID查询用户信息
     */
    UserVO getUserById(Long id);

    /**
     * 更新用户信息
     */
    boolean updateUser(Long id, UserVO userVO);

    /**
     * 修改密码
     */
    boolean changePassword(Long id, String oldPassword, String newPassword);

    /**
     * 检查用户名是否存在
     */
    boolean checkUsernameExists(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean checkEmailExists(String email);
}
