package com.examsystem.service;

import com.examsystem.dto.ExamResultVO;
import com.examsystem.dto.ExamSubmitRequest;
import com.examsystem.entity.Paper;

import java.util.List;

/**
 * 考试业务接口 — 负责学生考试相关功能：
 * 查看可用/即将开始的试卷、开始考试、获取试题、提交答卷、查看成绩、考试历史。
 * 考试基于试卷（Paper）进行，试卷状态为"已发布"且满足时间条件的才可作答。
 * 学生只能在已选修的课程范围内参加考试。
 */
public interface ExamService {

    /**
     * 获取学生当前可参加的考试列表（已发布、时间有效、且未完成）。
     * 筛选条件：status=1（已发布）、start_date已到或为空、end_date未过或为空、
     * 学生在t_exam_record中没有status=1（已完成）的记录。
     *
     * @param studentId 学生ID
     * @return 可参加的试卷列表
     */
    List<Paper> getAvailableExams(Long studentId);

    /**
     * 获取即将开始的考试列表（已发布但start_date尚未到达）。
     *
     * @param studentId 学生ID
     * @return 即将开始的试卷列表
     */
    List<Paper> getUpcomingExams(Long studentId);

    /**
     * 开始一场考试。校验试卷状态、时间范围、是否已完成后，创建一条status=0的考试记录。
     *
     * @param studentId 学生ID
     * @param paperId   试卷ID
     * @return 新创建的考试记录ID
     * @throws BusinessException 如果试卷不可作答、未开始、已截止、或已完成
     */
    Long startExam(Long studentId, Long paperId);

    /**
     * 获取考试试题列表。每道题目的answer和analysis字段会被清空（防止提前泄题）。
     * 判断题（type=3）若无选项则自动生成"对/错"默认选项。
     *
     * @param examRecordId 考试记录ID
     * @param studentId    学生ID（用于权限校验）
     * @return 试题列表（不含答案和解析）
     * @throws BusinessException 如果无权访问或考试已提交
     */
    List<com.examsystem.entity.Question> getExamQuestions(Long examRecordId, Long studentId);

    /**
     * 提交考试答卷。对每道题进行自动评分，批量保存答案，更新考试记录状态和总分。
     * 评分规则：
     * - 单选(1)、判断(3)：精确匹配（忽略大小写）
     * - 多选(2)：排序后比较（如"AC"和"CA"视为相同）
     * - 填空(4)、简答(5)：精确匹配
     *
     * @param examRecordId 考试记录ID
     * @param studentId    学生ID
     * @param request      包含各题答案的提交请求
     * @return 考试结果（总分、正确数、各题详情）
     * @throws BusinessException 如果无权访问或已提交
     */
    ExamResultVO submitExam(Long examRecordId, Long studentId, ExamSubmitRequest request);

    /**
     * 查看考试结果详情。只有已提交（status=1）的考试才能查看。
     *
     * @param examRecordId 考试记录ID
     * @param studentId    学生ID
     * @return 考试结果（含每道题的得分、正确答案、解析）
     * @throws BusinessException 如果无权查看或未提交
     */
    ExamResultVO getExamResult(Long examRecordId, Long studentId);

    /**
     * 分页查询学生的考试历史记录。
     *
     * @param studentId 学生ID
     * @param page      页码（从1开始）
     * @param pageSize  每页条数
     * @return 考试记录列表
     */
    List<com.examsystem.entity.ExamRecord> getExamHistory(Long studentId, Integer page, Integer pageSize);

    /**
     * 统计学生的考试历史总数。
     *
     * @param studentId 学生ID
     * @return 考试记录总数
     */
    Long countExamHistory(Long studentId);
}
