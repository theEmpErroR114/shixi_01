package com.examsystem.enums;

import lombok.Getter;

/**
 * 题目类型枚举
 * 不同类型决定答题交互方式：选择、判断用选项按钮，填空简答用文本框
 */
@Getter
public enum QuestionTypeEnum {
    /** 单选题 — 从选项中选一个正确答案 */
    SINGLE_CHOICE(1, "单选题"),
    /** 多选题 — 从选项中选多个正确答案 */
    MULTI_CHOICE(2, "多选题"),
    /** 判断题 — 选择"对"或"错" */
    TRUE_FALSE(3, "判断题"),
    /** 填空题 — 手动输入答案文本 */
    FILL_BLANK(4, "填空题"),
    /** 简答题 — 手动输入较长的答案文本 */
    SHORT_ANSWER(5, "简答题");

    /** 类型编码（存入数据库的整数值） */
    private final int code;
    /** 类型中文名称 */
    private final String name;

    QuestionTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
