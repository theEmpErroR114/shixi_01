package com.examsystem.enums;

import lombok.Getter;

/**
 * 题目难度枚举
 * 数据库中存储整数 code，前端展示中文 name
 */
@Getter
public enum DifficultyEnum {
    /** 容易 */
    EASY(1, "易"),
    /** 中等 */
    MEDIUM(2, "中"),
    /** 困难 */
    HARD(3, "难");

    /** 难度编码（存入数据库的整数值） */
    private final int code;
    /** 难度中文名称 */
    private final String name;

    DifficultyEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
