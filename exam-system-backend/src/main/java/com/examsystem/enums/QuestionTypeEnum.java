package com.examsystem.enums;

import lombok.Getter;

@Getter
public enum QuestionTypeEnum {
    SINGLE_CHOICE(1, "单选题"),
    MULTI_CHOICE(2, "多选题"),
    TRUE_FALSE(3, "判断题"),
    FILL_BLANK(4, "填空题"),
    SHORT_ANSWER(5, "简答题");

    private final int code;
    private final String name;

    QuestionTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
