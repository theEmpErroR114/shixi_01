package com.examsystem.enums;

import lombok.Getter;

@Getter
public enum DifficultyEnum {
    EASY(1, "易"),
    MEDIUM(2, "中"),
    HARD(3, "难");

    private final int code;
    private final String name;

    DifficultyEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
