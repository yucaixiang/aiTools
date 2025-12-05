-- 示例数据脚本 - 用于开发测试

USE tool_recommend;

-- 插入示例工具数据
INSERT INTO `tool` (`name`, `slug`, `tagline`, `description`, `logo_url`, `website_url`, `category_id`, `pricing_model`, `starting_price`, `launch_date`, `status`, `view_count`, `favorite_count`, `upvote_count`, `average_rating`) VALUES
('Notion', 'notion', '一站式协作工作空间', '# Notion介绍\n\nNotion是一个集笔记、知识库、数据表格、看板、日历等多种功能于一体的协作工具。\n\n## 主要功能\n- 笔记和文档\n- 数据库和表格\n- 项目管理\n- 团队协作\n- 模板市场', 'https://cdn.example.com/notion-logo.png', 'https://notion.so', 3, 'FREEMIUM', 0, '2016-03-15', 1, 125600, 8920, 6543, 4.8),

('VS Code', 'vscode', '微软开源的代码编辑器', 'Visual Studio Code是一款免费、开源的现代化代码编辑器，支持多种编程语言。', 'https://cdn.example.com/vscode-logo.png', 'https://code.visualstudio.com', 1, 'FREE', 0, '2015-04-29', 1, 450000, 25000, 18000, 4.9),

('Figma', 'figma', '协作式UI设计工具', 'Figma是一款基于浏览器的UI设计工具，支持实时协作，被全球设计团队广泛使用。', 'https://cdn.example.com/figma-logo.png', 'https://figma.com', 2, 'FREEMIUM', 0, '2016-09-27', 1, 320000, 15000, 12000, 4.7),

('ChatGPT', 'chatgpt', 'OpenAI的AI对话助手', 'ChatGPT是一个基于GPT模型的AI对话助手，可以回答问题、写作、编程等。', 'https://cdn.example.com/chatgpt-logo.png', 'https://chat.openai.com', 5, 'FREEMIUM', 20, '2022-11-30', 1, 890000, 45000, 38000, 4.6),

('GitHub', 'github', '全球最大的代码托管平台', 'GitHub是一个基于Git的代码托管平台，提供版本控制、协作开发、CI/CD等功能。', 'https://cdn.example.com/github-logo.png', 'https://github.com', 1, 'FREEMIUM', 0, '2008-04-10', 1, 1200000, 80000, 65000, 4.8),

('Obsidian', 'obsidian', '强大的本地笔记软件', 'Obsidian是一款基于Markdown的本地知识管理工具，支持双向链接和知识图谱。', 'https://cdn.example.com/obsidian-logo.png', 'https://obsidian.md', 3, 'FREEMIUM', 0, '2020-03-30', 1, 85000, 12000, 9800, 4.7),

('Postman', 'postman', 'API开发和测试工具', 'Postman是一个API开发协作平台，简化了API的设计、测试、文档和监控。', 'https://cdn.example.com/postman-logo.png', 'https://postman.com', 1, 'FREEMIUM', 0, '2014-05-23', 1, 280000, 18000, 15000, 4.6),

('Canva', 'canva', '在线设计平台', 'Canva是一个简单易用的在线设计平台，提供海量模板，适合非设计师使用。', 'https://cdn.example.com/canva-logo.png', 'https://canva.com', 2, 'FREEMIUM', 0, '2013-01-01', 1, 450000, 28000, 22000, 4.5),

('Trello', 'trello', '看板式项目管理工具', 'Trello采用看板方法管理项目，简单直观，适合团队协作和个人任务管理。', 'https://cdn.example.com/trello-logo.png', 'https://trello.com', 3, 'FREEMIUM', 0, '2011-09-13', 1, 380000, 22000, 18000, 4.4),

('Slack', 'slack', '团队沟通协作平台', 'Slack是一个企业级的团队沟通工具，整合了消息、文件分享、视频会议等功能。', 'https://cdn.example.com/slack-logo.png', 'https://slack.com', 3, 'FREEMIUM', 0, '2013-08-01', 1, 520000, 32000, 28000, 4.6);

-- 为工具添加标签
INSERT INTO `tool_tag` (`tool_id`, `tag_id`) VALUES
(1, 1), (1, 3), (1, 6), (1, 8),  -- Notion: 免费、跨平台、团队协作、Web应用
(2, 1), (2, 2), (2, 3), (2, 9),  -- VS Code: 免费、开源、跨平台、桌面应用
(3, 3), (3, 6), (3, 8),          -- Figma: 跨平台、团队协作、Web应用
(4, 5), (4, 8),                   -- ChatGPT: 人工智能、Web应用
(5, 1), (5, 3), (5, 6), (5, 8),  -- GitHub: 免费、跨平台、团队协作、Web应用
(6, 3), (6, 7), (6, 9),          -- Obsidian: 跨平台、个人使用、桌面应用
(7, 1), (7, 3), (7, 8),          -- Postman: 免费、跨平台、Web应用
(8, 1), (8, 7), (8, 8),          -- Canva: 免费、个人使用、Web应用
(9, 1), (9, 6), (9, 8),          -- Trello: 免费、团队协作、Web应用
(10, 6, 8);                       -- Slack: 团队协作、Web应用

-- 插入测试用户
INSERT INTO `user` (`username`, `email`, `password_hash`, `nickname`, `role`) VALUES
('testuser', 'test@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjefxaWwQcGmJqcNqy6wEKfKIrfqQ2i', '测试用户', 'USER'),
('demo', 'demo@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjefxaWwQcGmJqcNqy6wEKfKIrfqQ2i', '演示账号', 'USER');

-- 插入示例评论
INSERT INTO `review` (`tool_id`, `user_id`, `rating`, `title`, `content`, `pros`, `cons`, `usage_duration`, `helpful_count`) VALUES
(1, 2, 5, '非常好用的笔记工具', 'Notion是我用过最好的笔记工具，功能强大且灵活。', '["界面简洁", "功能强大", "支持协作"]', '["学习曲线略陡", "有时加载慢"]', 'HALF_YEAR', 42),
(2, 2, 5, '最好的代码编辑器', 'VS Code轻量、快速、插件丰富，完全免费！', '["免费开源", "插件丰富", "性能好"]', '["插件太多容易卡"]', 'YEAR', 128),
(3, 3, 4, 'UI设计必备', 'Figma让团队协作变得简单，实时同步太方便了。', '["在线协作", "原型设计", "组件系统"]', '["免费版限制多", "离线无法使用"]', 'QUARTER', 65);

-- 插入用户行为数据
INSERT INTO `user_action` (`user_id`, `tool_id`, `action_type`) VALUES
(2, 1, 'VIEW'),
(2, 1, 'FAVORITE'),
(2, 2, 'VIEW'),
(2, 2, 'UPVOTE'),
(3, 1, 'VIEW'),
(3, 3, 'VIEW'),
(3, 3, 'FAVORITE');

-- 插入示例对话
INSERT INTO `conversation` (`session_id`, `user_id`, `message`, `role`, `recommended_tools`, `model_used`) VALUES
('sess-001', 2, '我想找一个好用的笔记工具', 'USER', NULL, NULL),
('sess-001', 2, '根据您的需求，我推荐以下笔记工具：Notion、Obsidian、为知笔记', 'ASSISTANT', '[1, 6]', 'qwen2.5:7b'),
('sess-002', 3, '有没有免费的设计工具推荐？', 'USER', NULL, NULL),
('sess-002', 3, '我为您推荐以下免费设计工具：Figma、Canva、Pixso', 'ASSISTANT', '[3, 8]', 'qwen2.5:7b');

-- 查询确认
SELECT 'Sample data inserted successfully!' as message;
SELECT COUNT(*) as tool_count FROM tool;
SELECT COUNT(*) as review_count FROM review;
SELECT COUNT(*) as conversation_count FROM conversation;
