-- 为 edu_chapter_content 表增加封面字段
ALTER TABLE edu_chapter_content ADD COLUMN cover_image VARCHAR(500) DEFAULT NULL COMMENT '封面图';
