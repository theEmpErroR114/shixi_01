package com.examsystem.enums;

import lombok.Getter;

/**
 * 试卷状态枚举
 * 草稿可编辑，发布后学生可参加，回收后学生不可参加
 */
@Getter
public enum PaperStatusEnum {
    /** 未发布（草稿状态，仅教师可见和编辑） */
    DRAFT(0, "未发布"),
    /** 已发布（学生可以查看和参加考试） */
    PUBLISHED(1, "已发布"),
    /** 已回收（已发布的试卷被撤回，学生不可继续参加） */
    RECALLED(2, "已回收");

    /** 状态编码（存入数据库的整数值） */
    private final int code;
    /** 状态中文名称 */
    private final String name;

    PaperStatusEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
