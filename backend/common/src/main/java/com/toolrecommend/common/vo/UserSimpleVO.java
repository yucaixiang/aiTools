package com.toolrecommend.common.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户简单视图对象
 *
 * @author Tool Recommend Team
 */
@Data
public class UserSimpleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 个人简介
     */
    private String bio;
}
