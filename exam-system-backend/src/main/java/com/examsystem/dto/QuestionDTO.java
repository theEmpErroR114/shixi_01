package com.examsystem.dto;

import com.examsystem.entity.QuestionOption;
import lombok.Data;
import java.util.List;

@Data
public class QuestionDTO {
    private Long courseId;
    private Integer questionType;
    private String content;
    private String answer;
    private String analysis;
    private Integer difficulty;
    private List<QuestionOption> options;
}
