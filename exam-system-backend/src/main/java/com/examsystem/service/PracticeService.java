package com.examsystem.service;

import com.examsystem.dto.PracticeConfigRequest;
import com.examsystem.dto.PracticeResultVO;
import com.examsystem.entity.PracticeRecord;
import com.examsystem.entity.Question;

import java.util.List;

/**
 * 练习业务接口 — 负责学生自主练习功能：
 * 随机生成练习题、提交答案并自动评分、查看练习历史。
 * 练习模式每次随机抽取指定数量的题目，与考试模式不同，练习不限制时间。
 * 学生只能在已选修的课程范围内练习。
 */
public interface PracticeService {

    /**
     * 根据配置随机生成练习题。题目从指定课程中随机抽取，返回时清空answer和analysis字段。
     * 判断题（type=3）若数据库无选项则自动生成"对/错"两个默认选项。
     *
     * @param studentId 学生ID
     * @param config    练习配置（课程ID、题目类型、难度、数量）
     * @return 随机抽取的题目列表（不含答案和解析）
     * @throws BusinessException 如果学生未选修该课程
     */
    List<Question> generatePractice(Long studentId, PracticeConfigRequest config);

    /**
     * 提交练习答案并获取反馈。自动评分并保存练习记录。
     * 评分规则：
     * - 单选(1)、判断(3)：精确匹配（忽略大小写）
     * - 多选(2)：答案标签排序后比较（如"AC"与"CA"视为相同）
     * - 填空(4)、简答(5)：精确匹配，返回参考答案供学生自行对照
     *
     * @param studentId     学生ID
     * @param questionId    题目ID
     * @param studentAnswer 学生答案（单选/判断为单个字母如"A"，多选为排序后的标签串如"AC"）
     * @return 练习结果（正误判定、正确答案、解析、选项列表）
     */
    PracticeResultVO submitAnswer(Long studentId, Long questionId, String studentAnswer);

    /**
     * 分页查询学生的练习历史记录。
     *
     * @param studentId 学生ID
     * @param courseId  课程ID筛选（可为null）
     * @param page      页码（从1开始）
     * @param pageSize  每页条数
     * @return 练习记录列表
     */
    List<PracticeRecord> getPracticeHistory(Long studentId, Long courseId, Integer page, Integer pageSize);

    /**
     * 统计学生的练习历史总数。
     *
     * @param studentId 学生ID
     * @param courseId  课程ID筛选
     * @return 练习记录总数
     */
    Long countPracticeHistory(Long studentId, Long courseId);
}
