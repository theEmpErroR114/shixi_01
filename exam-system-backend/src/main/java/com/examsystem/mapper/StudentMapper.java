package com.examsystem.mapper;

import com.examsystem.entity.Student;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学生 Mapper 接口
 * 定义对 t_student 表的数据库操作
 */
public interface StudentMapper {

    /**
     * 根据用户名查询学生（登录验证用）
     * @param username 学生用户名
     * @return 学生实体，未找到返回 null
     */
    Student findByUsername(@Param("username") String username);

    /**
     * 根据学生ID查询学生
     * @param studentId 学生ID
     * @return 学生实体，未找到返回 null
     */
    Student selectById(@Param("studentId") Long studentId);

    /**
     * 新增学生
     * @param student 学生实体对象
     * @return 受影响的行数
     */
    int insert(Student student);

    /**
     * 更新学生信息
     * @param student 学生实体对象
     * @return 受影响的行数
     */
    int update(Student student);

    /**
     * 更新学生账号状态（启用/禁用）
     * @param studentId 学生ID
     * @param status    账号状态（1=启用, 0=禁用）
     * @return 受影响的行数
     */
    int updateStatus(@Param("studentId") Long studentId, @Param("status") Integer status);

    /**
     * 修改学生密码
     * @param studentId 学生ID
     * @param password  新密码（BCrypt加密后的密文）
     * @return 受影响的行数
     */
    int updatePassword(@Param("studentId") Long studentId, @Param("password") String password);

    /**
     * 根据关键字和筛选条件分页查询学生列表（管理员用）
     * @param keyword   关键字（模糊匹配姓名或用户名）
     * @param className 班级名（null 表示不限）
     * @param status    账号状态（null 表示不限）
     * @param offset    偏移量
     * @param limit     每页条数
     * @return 学生列表
     */
    List<Student> findByKeyword(@Param("keyword") String keyword, @Param("className") String className,
                                @Param("status") Integer status, @Param("offset") Integer offset,
                                @Param("limit") Integer limit);

    /**
     * 统计符合条件的学生总数
     * @param keyword   关键字
     * @param className 班级名
     * @param status    账号状态
     * @return 学生总数
     */
    Long countByKeyword(@Param("keyword") String keyword, @Param("className") String className,
                        @Param("status") Integer status);

    /**
     * 查询学生统计信息（含练习次数、考试次数、平均分等，管理员查看所有学生统计）
     * @param keyword 关键字（模糊匹配姓名或用户名）
     * @param offset  偏移量
     * @param limit   每页条数
     * @return 包含统计字段的 Map 列表
     */
    List<java.util.Map<String, Object>> selectStudentStats(@Param("keyword") String keyword,
                                                            @Param("offset") Integer offset,
                                                            @Param("limit") Integer limit);

    /**
     * 查询单个学生的详细统计信息
     * @param studentId 学生ID
     * @return 包含详细统计字段的 Map 列表
     */
    List<java.util.Map<String, Object>> selectStudentStatsByStudentId(@Param("studentId") Long studentId);

    /**
     * 根据课程范围查询学生统计信息（教师用，仅查看自己课程的学生）
     * @param keyword   关键字
     * @param courseIds 教师所属的课程ID列表
     * @param offset    偏移量
     * @param limit     每页条数
     * @return 包含统计字段的 Map 列表
     */
    List<java.util.Map<String, Object>> selectStudentStatsByCourseIds(@Param("keyword") String keyword,
                                                                       @Param("courseIds") List<Long> courseIds,
                                                                       @Param("offset") Integer offset,
                                                                       @Param("limit") Integer limit);

    /**
     * 统计指定课程范围内的学生总数（教师用）
     * @param keyword   关键字
     * @param courseIds 教师所属的课程ID列表
     * @return 学生总数
     */
    Long countByCourseIds(@Param("keyword") String keyword, @Param("courseIds") List<Long> courseIds);

    /**
     * 根据学生ID删除学生（删除前需先清理课程关联、练习和考试记录中的引用）
     * @param studentId 学生ID
     * @return 受影响的行数
     */
    int deleteById(@Param("studentId") Long studentId);
}
