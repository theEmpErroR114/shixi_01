package com.examsystem.mapper;

import com.examsystem.entity.Question;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题目 Mapper 接口
 * 定义对 t_question 表的数据库操作
 */
public interface QuestionMapper {

    /**
     * 根据筛选条件分页查询题目（管理员用，不限课程范围）
     * @param courseId     课程ID（null 表示不限）
     * @param questionType 题目类型（null 表示不限）
     * @param difficulty   难度（null 表示不限）
     * @param keyword      关键字（模糊匹配题目内容）
     * @param offset       偏移量
     * @param limit        每页条数
     * @return 题目列表
     */
    List<Question> findByFilters(@Param("courseId") Long courseId,
                                 @Param("questionType") Integer questionType, @Param("difficulty") Integer difficulty,
                                 @Param("keyword") String keyword, @Param("offset") Integer offset,
                                 @Param("limit") Integer limit);

    /**
     * 统计符合条件的题目总数（管理员用）
     * @param courseId     课程ID
     * @param questionType 题目类型
     * @param difficulty   难度
     * @param keyword      关键字
     * @return 题目总数
     */
    Long countByFilters(@Param("courseId") Long courseId,
                        @Param("questionType") Integer questionType, @Param("difficulty") Integer difficulty,
                        @Param("keyword") String keyword);

    /**
     * 根据筛选条件和课程范围分页查询题目（教师用，仅查询自己所属课程的题目）
     * @param courseId     课程ID
     * @param questionType 题目类型
     * @param difficulty   难度
     * @param keyword      关键字
     * @param offset       偏移量
     * @param limit        每页条数
     * @param courseIds    教师所属的课程ID列表
     * @return 题目列表
     */
    List<Question> findByFiltersAndCourseIds(@Param("courseId") Long courseId,
                                             @Param("questionType") Integer questionType, @Param("difficulty") Integer difficulty,
                                             @Param("keyword") String keyword, @Param("offset") Integer offset,
                                             @Param("limit") Integer limit, @Param("courseIds") List<Long> courseIds);

    /**
     * 统计符合条件的题目总数（教师用，限制课程范围）
     * @param courseId     课程ID
     * @param questionType 题目类型
     * @param difficulty   难度
     * @param keyword      关键字
     * @param courseIds    教师所属的课程ID列表
     * @return 题目总数
     */
    Long countByFiltersAndCourseIds(@Param("courseId") Long courseId,
                                     @Param("questionType") Integer questionType, @Param("difficulty") Integer difficulty,
                                     @Param("keyword") String keyword, @Param("courseIds") List<Long> courseIds);

    /**
     * 随机抽取指定数量的题目（练习模式或自动组卷时使用）
     * @param courseId     课程ID
     * @param questionType 题目类型（null 表示不限）
     * @param difficulty   难度（null 表示不限）
     * @param count        抽取数量
     * @return 随机题目列表
     */
    List<Question> findRandom(@Param("courseId") Long courseId, @Param("questionType") Integer questionType,
                              @Param("difficulty") Integer difficulty, @Param("count") Integer count);

    /**
     * 根据题目ID查询题目（不含选项，选项通过 QuestionOptionMapper 单独查询）
     * @param questionId 题目ID
     * @return 题目实体，未找到返回 null
     */
    Question selectById(@Param("questionId") Long questionId);

    /**
     * 新增题目
     * @param question 题目实体对象
     * @return 受影响的行数
     */
    int insert(Question question);

    /**
     * 更新题目信息
     * @param question 题目实体对象
     * @return 受影响的行数
     */
    int update(Question question);

    /**
     * 根据题目ID删除题目（需先清理 t_paper_question、t_practice_record、t_exam_answer、t_question_option 中的引用）
     * @param questionId 题目ID
     * @return 受影响的行数
     */
    int deleteById(@Param("questionId") Long questionId);
}
