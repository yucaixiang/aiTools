package com.toolrecommend.common.exception;

/**
 * 未授权异常
 *
 * @author Tool Recommend Team
 */
public class UnauthorizedException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public UnauthorizedException(String message) {
        super(401, message);
    }

    public UnauthorizedException() {
        super(401, "未授权，请先登录");
    }
}
