-- 迁移脚本：移除 teacher_id 外键约束
-- 题目和试卷改为与课程绑定，老师删除后不影响数据
-- 执行方式: mysql -u root -p exam_system < migrate_remove_teacher_fk.sql

ALTER TABLE t_question DROP FOREIGN KEY IF EXISTS fk_question_teacher;
ALTER TABLE t_paper DROP FOREIGN KEY IF EXISTS fk_paper_teacher;

-- 验证
SHOW CREATE TABLE t_question\G
SHOW CREATE TABLE t_paper\G
