-- Migration: Add start_date and end_date to t_paper
-- Run this against your existing exam_system database if t_paper already exists
ALTER TABLE t_paper
    ADD COLUMN start_date DATETIME NULL COMMENT '考试开始日期',
    ADD COLUMN end_date   DATETIME NULL COMMENT '考试截止日期';
