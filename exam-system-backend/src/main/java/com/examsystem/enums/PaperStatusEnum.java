package com.examsystem.enums;

import lombok.Getter;

@Getter
public enum PaperStatusEnum {
    DRAFT(0, "未发布"),
    PUBLISHED(1, "已发布"),
    RECALLED(2, "已回收");

    private final int code;
    private final String name;

    PaperStatusEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
