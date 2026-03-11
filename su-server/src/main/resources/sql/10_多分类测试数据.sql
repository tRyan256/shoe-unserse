-- 多分类测试数据
-- 为现有鞋子添加多分类关联测试数据

-- 注意：请根据实际的鞋子ID和分类ID进行调整
-- 假设分类ID分布如下：
-- 品牌分类(type=1): Nike(1), Adidas(2), New Balance(3), Puma(4), Converse(5)
-- 风格分类(type=2): 篮球鞋(11), 跑步鞋(12), 休闲鞋(13), 板鞋(14), 足球鞋(15)

-- 示例: Air Jordan 1 同时关联"Nike"品牌(id=1)和"篮球鞋"风格(id=11)
-- 请根据实际情况调整shoe_id和category_id的值

INSERT INTO shoe_category (shoe_id, category_id) VALUES
-- 假设鞋子1是Nike篮球鞋
(1, 1),   -- Nike品牌
(1, 11),  -- 篮球鞋风格

-- 假设鞋子2是Nike篮球鞋(另一款)
(2, 1),   -- Nike品牌
(2, 11),  -- 篮球鞋风格

-- 假设鞋子3是Nike板鞋
(3, 1),   -- Nike品牌
(3, 14),  -- 板鞋风格

-- 假设鞋子4是Nike休闲鞋
(4, 1),   -- Nike品牌
(4, 13),  -- 休闲鞋风格

-- 假设鞋子5是Adidas跑步鞋
(5, 2),   -- Adidas品牌
(5, 12),  -- 跑步鞋风格

-- 假设鞋子6是Adidas篮球鞋
(6, 2),   -- Adidas品牌
(6, 11),  -- 篮球鞋风格

-- 假设鞋子7是New Balance跑步鞋
(7, 3),   -- New Balance品牌
(7, 12),  -- 跑步鞋风格

-- 假设鞋子8是Puma足球鞋
(8, 4),   -- Puma品牌
(8, 15),  -- 足球鞋风格

-- 假设鞋子9是Converse板鞋
(9, 5),   -- Converse品牌
(9, 14),  -- 板鞋风格

-- 假设鞋子10是Nike跑步鞋和休闲鞋混合(多分类示例)
(10, 1),  -- Nike品牌
(10, 12), -- 跑步鞋风格
(10, 13)  -- 休闲鞋风格(一款鞋可以有多个风格标签)

ON DUPLICATE KEY UPDATE category_id = VALUES(category_id);

-- 提示：
-- 1. 执行此脚本前，请先执行 09_创建鞋子分类关联表.sql
-- 2. 请根据实际的鞋子数据和分类数据调整上述SQL
-- 3. 可以使用以下查询验证数据：
--    SELECT s.id, s.name, GROUP_CONCAT(c.name) as categories
--    FROM shoe s
--    LEFT JOIN shoe_category sc ON s.id = sc.shoe_id
--    LEFT JOIN category c ON sc.category_id = c.id
--    GROUP BY s.id, s.name;
