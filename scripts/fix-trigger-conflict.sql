-- ====================================
-- 修复触发器冲突问题
-- ====================================
-- 问题：after_review_insert 触发器与应用层代码都在更新 reply_count，导致冲突
-- 解决：删除触发器中更新 reply_count 的逻辑，改为只在应用层处理
-- ====================================

USE `tool_recommend`;

-- 1. 删除旧的 after_review_insert 触发器
DROP TRIGGER IF EXISTS `after_review_insert`;

-- 2. 重新创建触发器，只更新 tool 的 review_count，不更新 review 的 reply_count
DELIMITER //
CREATE TRIGGER `after_review_insert` AFTER INSERT ON `review`
FOR EACH ROW
BEGIN
  -- 只有顶级评论才更新工具的评论数
  IF NEW.`parent_id` IS NULL THEN
    UPDATE `tool`
    SET `review_count` = `review_count` + 1
    WHERE `id` = NEW.`tool_id`;
  END IF;
  -- 注意：回复的 reply_count 更新已移至应用层代码（ReviewServiceImpl）
END//
DELIMITER ;

-- 3. 同样需要修复 after_review_delete 触发器
DROP TRIGGER IF EXISTS `after_review_delete`;

DELIMITER //
CREATE TRIGGER `after_review_delete` AFTER DELETE ON `review`
FOR EACH ROW
BEGIN
  -- 只有顶级评论才更新工具的评论数
  IF OLD.`parent_id` IS NULL THEN
    UPDATE `tool`
    SET `review_count` = GREATEST(`review_count` - 1, 0)
    WHERE `id` = OLD.`tool_id`;
  END IF;
  -- 注意：回复的 reply_count 更新已移至应用层代码（ReviewServiceImpl）
END//
DELIMITER ;

-- ====================================
-- 验证触发器
-- ====================================
SHOW TRIGGERS WHERE `Table` = 'review';
