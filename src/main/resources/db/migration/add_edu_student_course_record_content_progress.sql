-- 学生课程学习记录表：增加内容学习进度 JSON 字段
-- 存储格式示例：{"1":1,"2":1} 表示内容ID 1、2 已学习，值为 1 表示已学习，用于回显与计算课程学习进度
ALTER TABLE edu_student_course_record
ADD COLUMN content_progress TEXT DEFAULT NULL COMMENT '内容学习进度JSON，key为内容ID，value为1表示已学习';
