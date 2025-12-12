-- 数据库迁移脚本：拆分评分和评论功能
-- 执行日期：2025-01-XX
-- 说明：
-- 1. 创建rating表用于存储评分（一个用户对一个工具只能打一次分）
-- 2. 修改review表支持多级回复功能
-- 3. 迁移现有的rating数据到新的rating表

USE tool_recommend;

-- ====================================
-- 步骤1: 创建rating评分表
-- ====================================
CREATE TABLE IF NOT EXISTS `rating` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评分ID',
  `tool_id` BIGINT NOT NULL COMMENT '工具ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `score` TINYINT NOT NULL COMMENT '评分1-5',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_tool_user` (`tool_id`, `user_id`) COMMENT '一个用户对一个工具只能打一次分',
  INDEX `idx_tool_id` (`tool_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_created_at` (`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工具评分表';

-- ====================================
-- 步骤2: 迁移现有的rating数据
-- ====================================
-- 从review表中迁移评分数据到rating表
INSERT INTO `rating` (`tool_id`, `user_id`, `score`, `created_at`, `updated_at`)
SELECT
  `tool_id`,
  `user_id`,
  `rating` as `score`,
  `created_at`,
  `updated_at`
FROM `review`
WHERE `rating` IS NOT NULL
ON DUPLICATE KEY UPDATE
  `score` = VALUES(`score`),
  `updated_at` = VALUES(`updated_at`);

-- ====================================
-- 步骤3: 修改review表，添加回复功能
-- ====================================
-- 添加parent_id字段用于支持回复
ALTER TABLE `review`
ADD COLUMN `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID，NULL表示顶级评论' AFTER `user_id`,
ADD INDEX `idx_parent_id` (`parent_id`);

-- 添加reply_count字段记录回复数
ALTER TABLE `review`
ADD COLUMN `reply_count` INT DEFAULT 0 COMMENT '回复数' AFTER `helpful_count`;

-- 移除rating字段（已迁移到rating表）
ALTER TABLE `review`
DROP COLUMN `rating`;

-- 移除旧的唯一索引（因为现在支持多条评论）
ALTER TABLE `review`
DROP INDEX `uk_tool_user`;

-- 添加新的索引
ALTER TABLE `review`
ADD INDEX `idx_tool_parent` (`tool_id`, `parent_id`, `created_at` DESC) COMMENT '查询某工具的顶级评论或某评论的回复';

-- ====================================
-- 步骤4: 创建统计触发器（可选）
-- ====================================
-- 注意：触发器可能影响性能，建议使用应用层代码更新统计

-- 创建rating后更新tool的average_rating
DELIMITER //
CREATE TRIGGER `after_rating_insert` AFTER INSERT ON `rating`
FOR EACH ROW
BEGIN
  UPDATE `tool`
  SET
    `average_rating` = (
      SELECT AVG(`score`)
      FROM `rating`
      WHERE `tool_id` = NEW.`tool_id`
    )
  WHERE `id` = NEW.`tool_id`;
END//

CREATE TRIGGER `after_rating_update` AFTER UPDATE ON `rating`
FOR EACH ROW
BEGIN
  UPDATE `tool`
  SET
    `average_rating` = (
      SELECT AVG(`score`)
      FROM `rating`
      WHERE `tool_id` = NEW.`tool_id`
    )
  WHERE `id` = NEW.`tool_id`;
END//

CREATE TRIGGER `after_rating_delete` AFTER DELETE ON `rating`
FOR EACH ROW
BEGIN
  UPDATE `tool`
  SET
    `average_rating` = COALESCE((
      SELECT AVG(`score`)
      FROM `rating`
      WHERE `tool_id` = OLD.`tool_id`
    ), 0)
  WHERE `id` = OLD.`tool_id`;
END//

-- 创建review后更新tool的review_count（只统计顶级评论）
CREATE TRIGGER `after_review_insert` AFTER INSERT ON `review`
FOR EACH ROW
BEGIN
  IF NEW.`parent_id` IS NULL THEN
    UPDATE `tool`
    SET `review_count` = `review_count` + 1
    WHERE `id` = NEW.`tool_id`;
  ELSE
    UPDATE `review`
    SET `reply_count` = `reply_count` + 1
    WHERE `id` = NEW.`parent_id`;
  END IF;
END//

CREATE TRIGGER `after_review_delete` AFTER DELETE ON `review`
FOR EACH ROW
BEGIN
  IF OLD.`parent_id` IS NULL THEN
    UPDATE `tool`
    SET `review_count` = GREATEST(`review_count` - 1, 0)
    WHERE `id` = OLD.`tool_id`;
  ELSE
    UPDATE `review`
    SET `reply_count` = GREATEST(`reply_count` - 1, 0)
    WHERE `id` = OLD.`parent_id`;
  END IF;
END//

DELIMITER ;

-- ====================================
-- 验证迁移结果
-- ====================================
-- 查看rating表的数据量
SELECT COUNT(*) as rating_count FROM `rating`;

-- 查看每个工具的平均评分
SELECT
  t.id,
  t.name,
  t.average_rating as old_rating,
  (SELECT AVG(score) FROM rating WHERE tool_id = t.id) as new_rating,
  (SELECT COUNT(*) FROM rating WHERE tool_id = t.id) as rating_count
FROM tool t
LIMIT 10;

-- 查看review表结构
SHOW CREATE TABLE `review`;

-- ====================================
-- 回滚脚本（如果需要）
-- ====================================
/*
-- 删除触发器
DROP TRIGGER IF EXISTS `after_rating_insert`;
DROP TRIGGER IF EXISTS `after_rating_update`;
DROP TRIGGER IF EXISTS `after_rating_delete`;
DROP TRIGGER IF EXISTS `after_review_insert`;
DROP TRIGGER IF EXISTS `after_review_delete`;

-- 恢复review表的rating字段
ALTER TABLE `review`
ADD COLUMN `rating` TINYINT COMMENT '评分1-5' AFTER `user_id`,
DROP COLUMN `parent_id`,
DROP COLUMN `reply_count`,
DROP INDEX `idx_parent_id`,
DROP INDEX `idx_tool_parent`,
ADD UNIQUE KEY `uk_tool_user` (`tool_id`, `user_id`);

-- 从rating表恢复数据到review表
UPDATE `review` r
INNER JOIN `rating` ra ON r.tool_id = ra.tool_id AND r.user_id = ra.user_id
SET r.rating = ra.score;

-- 删除rating表
DROP TABLE IF EXISTS `rating`;
*/
