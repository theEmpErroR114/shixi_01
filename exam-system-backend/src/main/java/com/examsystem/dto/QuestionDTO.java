package com.examsystem.dto;

import com.examsystem.entity.QuestionOption;
import lombok.Data;
import java.util.List;

/**
 * 题目创建/编辑请求 DTO
 * 教师创建或编辑题目时使用，包含题目内容和选项
 */
@Data
public class QuestionDTO {
    /** 所属课程ID */
    private Long courseId;
    /** 题目类型：1=单选, 2=多选, 3=判断, 4=填空, 5=简答 */
    private Integer questionType;
    /** 题目内容（题干） */
    private String content;
    /** 正确答案（单选为"A"，多选为"A,C"，判断为"对"/"错"） */
    private String answer;
    /** 题目解析 */
    private String analysis;
    /** 难度：1=易, 2=中, 3=难 */
    private Integer difficulty;
    /** 选项列表（单选和多选必须有至少2个选项，判断填空简答可为空） */
    private List<QuestionOption> options;
}
