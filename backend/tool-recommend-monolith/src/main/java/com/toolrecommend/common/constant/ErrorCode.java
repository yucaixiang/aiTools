package com.toolrecommend.common.constant;

/**
 * 错误码常量
 *
 * @author Tool Recommend Team
 */
public class ErrorCode {

    // 系统错误
    public static final int SYSTEM_ERROR = 500;
    public static final int PARAM_ERROR = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;

    // 用户相关错误 (1xxx)
    public static final int USER_NOT_FOUND = 1001;
    public static final int USER_ALREADY_EXISTS = 1002;
    public static final int PASSWORD_ERROR = 1003;
    public static final int TOKEN_EXPIRED = 1004;
    public static final int TOKEN_INVALID = 1005;

    // 工具相关错误 (2xxx)
    public static final int TOOL_NOT_FOUND = 2001;
    public static final int TOOL_SLUG_EXISTS = 2002;
    public static final int TOOL_STATUS_ERROR = 2003;

    // 评论相关错误 (3xxx)
    public static final int REVIEW_EXISTS = 3001;
    public static final int REVIEW_NOT_FOUND = 3002;

    // AI相关错误 (4xxx)
    public static final int AI_SERVICE_ERROR = 4001;
    public static final int AI_MODEL_NOT_AVAILABLE = 4002;

    private ErrorCode() {
    }
}
