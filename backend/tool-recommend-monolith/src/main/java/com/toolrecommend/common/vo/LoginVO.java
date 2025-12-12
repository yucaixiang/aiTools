package com.toolrecommend.common.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录返回VO
 *
 * @author Tool Recommend Team
 */
@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 访问Token
     */
    private String accessToken;

    /**
     * 刷新Token
     */
    private String refreshToken;

    /**
     * Token类型
     */
    private String tokenType = "Bearer";

    /**
     * 过期时间（秒）
     */
    private Long expiresIn;

    /**
     * 用户信息
     */
    private UserVO user;

    public LoginVO() {
    }

    public LoginVO(String accessToken, String refreshToken, Long expiresIn, UserVO user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }
}
