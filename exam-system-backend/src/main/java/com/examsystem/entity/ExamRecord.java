package com.examsystem.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试记录实体类，对应 t_exam_record 表
 * 记录学生每次参加考试的完整信息，包括交卷时间、总分等
 */
@Data
public class ExamRecord {
    /** 考试记录ID（主键，自增） */
    private Long examRecordId;
    /** 学生ID */
    private Long studentId;
    /** 试卷ID */
    private Long paperId;
    /** 考试总分（交卷后由系统自动批改计算得出） */
    private BigDecimal totalScore;
    /** 开始考试的时间 */
    private LocalDateTime startTime;
    /** 交卷时间 */
    private LocalDateTime submitTime;
    /** 考试状态：0=进行中, 1=已提交 */
    private Integer status;

    // ========== 以下为非数据库字段，用于关联查询展示 ==========

    /** 试卷名称（关联查询填充） */
    private String paperName;
    /** 课程名称（关联查询填充） */
    private String courseName;
    /** 试卷限时（分钟，关联查询填充） */
    private Integer paperDuration;
    /** 试卷总分（关联查询填充） */
    private Integer paperTotalScore;
    /** 学生姓名（关联查询填充） */
    private String studentName;
    /** 学生班级（关联查询填充） */
    private String className;
    /** 该次考试的答案列表（关联查询填充） */
    private List<ExamAnswer> answers;
}
