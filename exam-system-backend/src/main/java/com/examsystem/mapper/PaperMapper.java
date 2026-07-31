package com.examsystem.mapper;

import com.examsystem.entity.Paper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 试卷 Mapper 接口
 * 定义对 t_paper 表的数据库操作
 */
public interface PaperMapper {

    /**
     * 根据状态和课程ID分页查询试卷（管理员用，不限课程范围）
     * @param status   试卷状态（null 表示查所有状态）
     * @param courseId 课程ID（null 表示不限课程）
     * @param offset   偏移量
     * @param limit    每页条数
     * @return 试卷列表
     */
    List<Paper> findByFilters(@Param("status") Integer status,
                              @Param("courseId") Long courseId, @Param("offset") Integer offset,
                              @Param("limit") Integer limit);

    /**
     * 统计符合条件的试卷总数（管理员用）
     * @param status   试卷状态
     * @param courseId 课程ID
     * @return 试卷总数
     */
    Long countByFilters(@Param("status") Integer status,
                        @Param("courseId") Long courseId);

    /**
     * 根据状态、课程ID和指定课程范围分页查询试卷（教师用，仅查询自己所属课程的试卷）
     * @param status    试卷状态
     * @param courseId  课程ID
     * @param offset    偏移量
     * @param limit     每页条数
     * @param courseIds 教师所属的课程ID列表
     * @return 试卷列表
     */
    List<Paper> findByFiltersAndCourseIds(@Param("status") Integer status,
                                          @Param("courseId") Long courseId, @Param("offset") Integer offset,
                                          @Param("limit") Integer limit, @Param("courseIds") List<Long> courseIds);

    /**
     * 统计符合条件的试卷总数（教师用，限制课程范围）
     * @param status    试卷状态
     * @param courseId  课程ID
     * @param courseIds 教师所属的课程ID列表
     * @return 试卷总数
     */
    Long countByFiltersAndCourseIds(@Param("status") Integer status,
                                     @Param("courseId") Long courseId, @Param("courseIds") List<Long> courseIds);

    /**
     * 根据试卷ID查询试卷
     * @param paperId 试卷ID
     * @return 试卷实体，未找到返回 null
     */
    Paper selectById(@Param("paperId") Long paperId);

    /**
     * 新增试卷
     * @param paper 试卷实体对象
     * @return 受影响的行数
     */
    int insert(Paper paper);

    /**
     * 更新试卷信息
     * @param paper 试卷实体对象
     * @return 受影响的行数
     */
    int update(Paper paper);

    /**
     * 更新试卷状态（发布/回收）
     * @param paperId 试卷ID
     * @param status  新的状态（0=未发布, 1=已发布, 2=已回收）
     * @return 受影响的行数
     */
    int updateStatus(@Param("paperId") Long paperId, @Param("status") Integer status);

    /**
     * 根据试卷ID删除试卷
     * @param paperId 试卷ID
     * @return 受影响的行数
     */
    int deleteById(@Param("paperId") Long paperId);

    /**
     * 查询学生可参加的考试列表（已发布、在有效日期内、学生所在课程的试卷）
     * @param courseIds 学生已选课程ID列表
     * @param now       当前时间
     * @return 可参加的试卷列表
     */
    List<Paper> findAvailableForStudent(@Param("courseIds") List<Long> courseIds,
                                        @Param("now") LocalDateTime now);

    /**
     * 查询学生即将开始的考试列表（已发布、开始日期尚未到达）
     * @param courseIds 学生已选课程ID列表
     * @param now       当前时间
     * @return 即将开始的试卷列表
     */
    List<Paper> findUpcomingForStudent(@Param("courseIds") List<Long> courseIds,
                                       @Param("now") LocalDateTime now);
}
