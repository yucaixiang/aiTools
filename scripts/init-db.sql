-- AI工具推荐系统数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS tool_recommend DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE tool_recommend;

-- 1. 工具表
CREATE TABLE IF NOT EXISTS `tool` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '工具ID',
  `name` VARCHAR(200) NOT NULL COMMENT '工具名称',
  `slug` VARCHAR(200) UNIQUE NOT NULL COMMENT 'URL友好标识',
  `tagline` VARCHAR(500) COMMENT '一句话介绍',
  `description` TEXT COMMENT '详细描述',
  `logo_url` VARCHAR(500) COMMENT 'Logo地址',
  `website_url` VARCHAR(500) COMMENT '官网地址',
  `download_url` VARCHAR(500) COMMENT '下载地址',
  `github_url` VARCHAR(500) COMMENT 'GitHub地址',
  `category_id` INT NOT NULL COMMENT '主分类ID',
  `pricing_model` ENUM('FREE', 'FREEMIUM', 'PAID', 'OPEN_SOURCE') DEFAULT 'FREE' COMMENT '定价模式',
  `starting_price` DECIMAL(10,2) COMMENT '起始价格',
  `launch_date` DATE COMMENT '发布日期',
  `maker_id` BIGINT COMMENT '创建者ID',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-草稿 1-已发布 2-已下架',
  `view_count` INT DEFAULT 0 COMMENT '浏览量',
  `favorite_count` INT DEFAULT 0 COMMENT '收藏数',
  `upvote_count` INT DEFAULT 0 COMMENT '点赞数',
  `review_count` INT DEFAULT 0 COMMENT '评论数',
  `average_rating` DECIMAL(3,2) DEFAULT 0.00 COMMENT '平均评分',
  `profile_completeness` DECIMAL(3,2) DEFAULT 0.00 COMMENT '信息完整度',
  `monthly_growth_rate` DECIMAL(5,4) DEFAULT 0.0000 COMMENT '月增长率',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_category_status` (`category_id`, `status`),
  INDEX `idx_status_launch` (`status`, `launch_date` DESC),
  INDEX `idx_rating` (`average_rating` DESC, `review_count` DESC),
  FULLTEXT INDEX `ft_search` (`name`, `tagline`, `description`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工具信息表';

-- 2. 分类表
CREATE TABLE IF NOT EXISTS `category` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `slug` VARCHAR(100) UNIQUE NOT NULL COMMENT 'URL标识',
  `parent_id` INT DEFAULT NULL COMMENT '父分类ID',
  `icon` VARCHAR(200) COMMENT '图标',
  `description` VARCHAR(500) COMMENT '描述',
  `sort_order` INT DEFAULT 0 COMMENT '排序权重',
  `tool_count` INT DEFAULT 0 COMMENT '工具数量',
  `is_active` TINYINT DEFAULT 1 COMMENT '是否启用',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_parent` (`parent_id`, `sort_order` DESC),
  INDEX `idx_active` (`is_active`, `sort_order` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- 3. 标签表
CREATE TABLE IF NOT EXISTS `tag` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
  `name` VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
  `slug` VARCHAR(50) UNIQUE NOT NULL COMMENT 'URL标识',
  `usage_count` INT DEFAULT 0 COMMENT '使用次数',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_usage` (`usage_count` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- 4. 工具标签关联表
CREATE TABLE IF NOT EXISTS `tool_tag` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `tool_id` BIGINT NOT NULL COMMENT '工具ID',
  `tag_id` INT NOT NULL COMMENT '标签ID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_tool_tag` (`tool_id`, `tag_id`),
  INDEX `idx_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工具标签关联表';

-- 5. 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
  `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
  `phone` VARCHAR(20) UNIQUE COMMENT '手机号',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `avatar_url` VARCHAR(500) COMMENT '头像URL',
  `nickname` VARCHAR(50) COMMENT '昵称',
  `bio` VARCHAR(500) COMMENT '个人简介',
  `role` ENUM('USER', 'MAKER', 'ADMIN', 'SUPER_ADMIN') DEFAULT 'USER' COMMENT '用户角色',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
  `last_login_at` TIMESTAMP COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) COMMENT '最后登录IP',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_email` (`email`),
  INDEX `idx_phone` (`phone`),
  INDEX `idx_status` (`status`, `created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 6. 用户行为表
CREATE TABLE IF NOT EXISTS `user_action` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `tool_id` BIGINT NOT NULL COMMENT '工具ID',
  `action_type` ENUM('VIEW', 'UPVOTE', 'FAVORITE', 'CLICK_WEBSITE', 'CLICK_DOWNLOAD') NOT NULL COMMENT '行为类型',
  `action_value` VARCHAR(200) COMMENT '行为值',
  `source` VARCHAR(50) COMMENT '来源',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_user_tool` (`user_id`, `tool_id`, `action_type`),
  INDEX `idx_tool_action` (`tool_id`, `action_type`, `created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为表';

-- 7. 评论表
CREATE TABLE IF NOT EXISTS `review` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `tool_id` BIGINT NOT NULL COMMENT '工具ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `rating` TINYINT NOT NULL COMMENT '评分1-5',
  `title` VARCHAR(200) COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容',
  `pros` JSON COMMENT '优点',
  `cons` JSON COMMENT '缺点',
  `usage_duration` ENUM('WEEK', 'MONTH', 'QUARTER', 'HALF_YEAR', 'YEAR') COMMENT '使用时长',
  `helpful_count` INT DEFAULT 0 COMMENT '有帮助数',
  `status` TINYINT DEFAULT 1 COMMENT '状态',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_tool_user` (`tool_id`, `user_id`),
  INDEX `idx_tool_status` (`tool_id`, `status`, `created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- 8. AI对话表
CREATE TABLE IF NOT EXISTS `conversation` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `session_id` VARCHAR(100) NOT NULL COMMENT '会话ID',
  `user_id` BIGINT COMMENT '用户ID',
  `message` TEXT NOT NULL COMMENT '消息内容',
  `role` ENUM('USER', 'ASSISTANT') NOT NULL COMMENT '角色',
  `recommended_tools` JSON COMMENT '推荐工具ID列表',
  `intent` VARCHAR(100) COMMENT '意图',
  `entities` JSON COMMENT '实体',
  `model_used` VARCHAR(50) COMMENT '使用的模型',
  `tokens_used` INT COMMENT 'Token消耗',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_session` (`session_id`, `created_at` ASC),
  INDEX `idx_user` (`user_id`, `created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话表';

-- 插入初始分类数据
INSERT INTO `category` (`name`, `slug`, `parent_id`, `icon`, `sort_order`) VALUES
('开发工具', 'developer-tools', NULL, 'icon-code', 100),
('设计工具', 'design-tools', NULL, 'icon-design', 90),
('生产力工具', 'productivity', NULL, 'icon-productivity', 80),
('营销工具', 'marketing', NULL, 'icon-marketing', 70),
('AI工具', 'ai-tools', NULL, 'icon-ai', 60),
('数据分析', 'analytics', NULL, 'icon-chart', 50);

-- 插入子分类
INSERT INTO `category` (`name`, `slug`, `parent_id`, `icon`, `sort_order`) VALUES
('代码编辑器', 'code-editors', 1, 'icon-editor', 10),
('版本控制', 'version-control', 1, 'icon-git', 9),
('UI设计', 'ui-design', 2, 'icon-ui', 10),
('原型工具', 'prototyping', 2, 'icon-prototype', 9),
('笔记工具', 'note-taking', 3, 'icon-note', 10),
('任务管理', 'task-management', 3, 'icon-task', 9);

-- 插入初始标签
INSERT INTO `tag` (`name`, `slug`) VALUES
('免费', 'free'),
('开源', 'open-source'),
('跨平台', 'cross-platform'),
('云服务', 'cloud-based'),
('人工智能', 'ai'),
('团队协作', 'collaboration'),
('个人使用', 'personal'),
('Web应用', 'web-app'),
('桌面应用', 'desktop-app'),
('移动应用', 'mobile-app');

-- 创建管理员账户 (密码: admin123，需要在应用中加密)
INSERT INTO `user` (`username`, `email`, `password_hash`, `nickname`, `role`, `status`) VALUES
('admin', 'admin@toolrecommend.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjefxaWwQcGmJqcNqy6wEKfKIrfqQ2i', '系统管理员', 'SUPER_ADMIN', 1);

-- 查询确认
SELECT 'Database initialized successfully!' as message;
SELECT COUNT(*) as category_count FROM category;
SELECT COUNT(*) as tag_count FROM tag;
SELECT COUNT(*) as user_count FROM user;
