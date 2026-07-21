-- ============================================================
-- 课程习题测验系统 - 数据库建表脚本
-- 数据库引擎: InnoDB, 字符集: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS exam_system
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE exam_system;

-- ==================== 账号体系 (3张表) ====================

-- 1. 管理员表
CREATE TABLE IF NOT EXISTS t_admin (
    admin_id    BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '管理员ID',
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录账号',
    password    VARCHAR(100) NOT NULL COMMENT '登录密码（BCrypt加密）',
    real_name   VARCHAR(50)  COMMENT '姓名',
    phone       VARCHAR(20)  COMMENT '联系电话',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 2. 老师表
CREATE TABLE IF NOT EXISTS t_teacher (
    teacher_id  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '老师ID',
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录账号',
    password    VARCHAR(100) NOT NULL COMMENT '登录密码（BCrypt加密）',
    real_name   VARCHAR(50)  COMMENT '姓名',
    gender      CHAR(1)      COMMENT '性别（M/F）',
    phone       VARCHAR(20)  COMMENT '联系电话',
    subject     VARCHAR(50)  COMMENT '所授科目',
    status      TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_by   BIGINT       COMMENT '创建该账号的管理员ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_teacher_admin FOREIGN KEY (create_by) REFERENCES t_admin (admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老师表';

-- 3. 学生表
CREATE TABLE IF NOT EXISTS t_student (
    student_id  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '学生ID',
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录账号',
    password    VARCHAR(100) NOT NULL COMMENT '登录密码（BCrypt加密）',
    real_name   VARCHAR(50)  COMMENT '姓名',
    gender      CHAR(1)      COMMENT '性别（M/F）',
    class_name  VARCHAR(50)  COMMENT '班级',
    phone       VARCHAR(20)  COMMENT '联系电话',
    status      TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_by   BIGINT       COMMENT '创建该账号的管理员ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_student_admin FOREIGN KEY (create_by) REFERENCES t_admin (admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

-- ==================== 教学内容 (5张表) ====================

-- 4. 课程表
CREATE TABLE IF NOT EXISTS t_course (
    course_id   BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '课程ID',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    description VARCHAR(255) COMMENT '课程描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 5. 习题表
CREATE TABLE IF NOT EXISTS t_question (
    question_id   BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '习题ID',
    course_id     BIGINT      NOT NULL COMMENT '所属课程ID',
    question_type TINYINT     NOT NULL COMMENT '题型：1单选 2多选 3判断 4填空 5简答',
    content       TEXT        NOT NULL COMMENT '题干内容',
    answer        VARCHAR(255) COMMENT '参考答案',
    analysis      TEXT        COMMENT '答案解析',
    difficulty    TINYINT DEFAULT 1 COMMENT '难度：1易 2中 3难',
    teacher_id    BIGINT      NULL COMMENT '出题老师ID（仅展示，不作为权限过滤）',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_question_course  FOREIGN KEY (course_id)  REFERENCES t_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='习题表';

-- 6. 习题选项表
CREATE TABLE IF NOT EXISTS t_question_option (
    option_id      BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '选项ID',
    question_id    BIGINT       NOT NULL COMMENT '所属习题ID',
    option_label   CHAR(1)      NOT NULL COMMENT '选项标号：A/B/C/D',
    option_content VARCHAR(255) NOT NULL COMMENT '选项内容',
    is_correct     TINYINT DEFAULT 0 COMMENT '是否为正确答案：1是 0否',
    CONSTRAINT fk_option_question FOREIGN KEY (question_id) REFERENCES t_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='习题选项表（选择题用）';

-- 7. 试卷表
CREATE TABLE IF NOT EXISTS t_paper (
    paper_id    BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '试卷ID',
    paper_name  VARCHAR(100) NOT NULL COMMENT '试卷名称',
    course_id   BIGINT       NOT NULL COMMENT '所属课程ID',
    teacher_id  BIGINT       NULL COMMENT '组卷老师ID（仅展示，不作为权限过滤）',
    total_score INT DEFAULT 100 COMMENT '试卷总分',
    duration    INT DEFAULT 60 COMMENT '考试时长（分钟）',
    status      TINYINT DEFAULT 0 COMMENT '状态：0未发布 1已发布 2已回收',
    start_date  DATETIME    NULL        COMMENT '考试开始日期（学生可答题的起始时间）',
    end_date    DATETIME    NULL        COMMENT '考试截止日期（学生答题的截止时间）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_paper_course  FOREIGN KEY (course_id)  REFERENCES t_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表';

-- 8. 试卷-习题关联表
CREATE TABLE IF NOT EXISTS t_paper_question (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    paper_id    BIGINT NOT NULL COMMENT '试卷ID',
    question_id BIGINT NOT NULL COMMENT '习题ID',
    score       INT DEFAULT 10 COMMENT '该题分值',
    sort_order  INT DEFAULT 1 COMMENT '题目顺序',
    CONSTRAINT fk_pq_paper    FOREIGN KEY (paper_id)    REFERENCES t_paper (paper_id),
    CONSTRAINT fk_pq_question FOREIGN KEY (question_id) REFERENCES t_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷-习题关联表';

-- ==================== 学习记录 (3张表) ====================

-- 9. 学生练习记录表
CREATE TABLE IF NOT EXISTS t_practice_record (
    record_id      BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    student_id     BIGINT NULL COMMENT '学生ID',
    question_id    BIGINT NOT NULL COMMENT '习题ID',
    student_answer VARCHAR(255) COMMENT '学生作答内容',
    is_correct     TINYINT COMMENT '是否正确：1正确 0错误',
    practice_time  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '练习时间',
    CONSTRAINT fk_practice_student  FOREIGN KEY (student_id)  REFERENCES t_student (student_id) ON DELETE SET NULL,
    CONSTRAINT fk_practice_question FOREIGN KEY (question_id) REFERENCES t_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生练习记录表';

-- 10. 学生测验记录表（主表）
CREATE TABLE IF NOT EXISTS t_exam_record (
    exam_record_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '测验记录ID',
    student_id     BIGINT NULL COMMENT '学生ID',
    paper_id       BIGINT NOT NULL COMMENT '试卷ID',
    total_score    DECIMAL(5,1) COMMENT '本次测验实际得分',
    start_time     DATETIME COMMENT '开始时间',
    submit_time    DATETIME COMMENT '交卷时间',
    status         TINYINT DEFAULT 0 COMMENT '状态：0进行中 1已交卷',
    CONSTRAINT fk_exam_student FOREIGN KEY (student_id) REFERENCES t_student (student_id) ON DELETE SET NULL,
    CONSTRAINT fk_exam_paper   FOREIGN KEY (paper_id)   REFERENCES t_paper (paper_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生测验记录表（主表）';

-- 11. 学生测验答题明细表
CREATE TABLE IF NOT EXISTS t_exam_answer (
    answer_id      BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '答题明细ID',
    exam_record_id BIGINT NOT NULL COMMENT '所属测验记录ID',
    question_id    BIGINT NOT NULL COMMENT '习题ID',
    student_answer VARCHAR(255) COMMENT '学生作答内容',
    is_correct     TINYINT COMMENT '是否正确：1正确 0错误',
    score          DECIMAL(5,1) COMMENT '该题得分',
    CONSTRAINT fk_answer_exam     FOREIGN KEY (exam_record_id) REFERENCES t_exam_record (exam_record_id),
    CONSTRAINT fk_answer_question FOREIGN KEY (question_id)     REFERENCES t_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生测验答题明细表';

-- ==================== 课程关联 (2张表) ====================

-- 12. 教师-课程关联表
CREATE TABLE IF NOT EXISTS t_teacher_course (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_id BIGINT NOT NULL,
    course_id  BIGINT NOT NULL,
    UNIQUE KEY uk_teacher_course (teacher_id, course_id),
    CONSTRAINT fk_tc_teacher FOREIGN KEY (teacher_id) REFERENCES t_teacher (teacher_id),
    CONSTRAINT fk_tc_course  FOREIGN KEY (course_id)  REFERENCES t_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师-课程关联表';

-- 13. 学生-课程关联表
CREATE TABLE IF NOT EXISTS t_student_course (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id  BIGINT NOT NULL,
    UNIQUE KEY uk_student_course (student_id, course_id),
    CONSTRAINT fk_sc_student FOREIGN KEY (student_id) REFERENCES t_student (student_id),
    CONSTRAINT fk_sc_course  FOREIGN KEY (course_id)  REFERENCES t_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生-课程关联表';
