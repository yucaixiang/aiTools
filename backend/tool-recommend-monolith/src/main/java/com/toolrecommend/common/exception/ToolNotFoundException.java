package com.toolrecommend.common.exception;

/**
 * 工具不存在异常
 *
 * @author Tool Recommend Team
 */
public class ToolNotFoundException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public ToolNotFoundException(String message) {
        super(404, message);
    }

    public ToolNotFoundException(Long toolId) {
        super(404, "工具不存在，ID: " + toolId);
    }
}
