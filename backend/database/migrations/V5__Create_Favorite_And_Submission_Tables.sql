-- 创建收藏表
CREATE TABLE IF NOT EXISTS `favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `tool_id` BIGINT NOT NULL COMMENT '工具ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_tool` (`user_id`, `tool_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_tool_id` (`tool_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- 创建工具提交表
CREATE TABLE IF NOT EXISTS `tool_submission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '提交ID',
    `user_id` BIGINT NOT NULL COMMENT '提交用户ID',
    `name` VARCHAR(100) NOT NULL COMMENT '工具名称',
    `tagline` VARCHAR(200) NOT NULL COMMENT '工具标语',
    `description` TEXT COMMENT '工具描述',
    `website_url` VARCHAR(500) NOT NULL COMMENT '网站URL',
    `logo_url` VARCHAR(500) COMMENT 'Logo URL',
    `category_id` BIGINT COMMENT '分类ID',
    `pricing_model` VARCHAR(20) COMMENT '定价模式',
    `launch_date` DATETIME COMMENT '发布日期',
    `status` INT NOT NULL DEFAULT 0 COMMENT '审核状态: 0-待审核 1-已通过 2-已拒绝',
    `review_comment` TEXT COMMENT '审核意见',
    `reviewed_at` DATETIME COMMENT '审核时间',
    `reviewed_by` BIGINT COMMENT '审核人ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工具提交表';

-- 创建代找/代开发请求表
CREATE TABLE IF NOT EXISTS `help_request` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '请求ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `request_type` VARCHAR(20) NOT NULL COMMENT '请求类型: FIND-代找工具 DEVELOP-代开发',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `description` TEXT NOT NULL COMMENT '需求描述',
    `budget` VARCHAR(100) COMMENT '预算范围',
    `contact` VARCHAR(200) COMMENT '联系方式',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态: 0-待处理 1-处理中 2-已完成 3-已关闭',
    `reply` TEXT COMMENT '回复内容',
    `replied_at` DATETIME COMMENT '回复时间',
    `replied_by` BIGINT COMMENT '回复人ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_request_type` (`request_type`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代找代开发请求表';
