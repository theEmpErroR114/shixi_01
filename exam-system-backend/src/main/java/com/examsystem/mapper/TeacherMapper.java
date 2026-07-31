package com.examsystem.mapper;

import com.examsystem.entity.Teacher;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 教师 Mapper 接口
 * 定义对 t_teacher 表的数据库操作
 */
public interface TeacherMapper {

    /**
     * 根据用户名查询教师（登录验证用）
     * @param username 教师用户名
     * @return 教师实体，未找到返回 null
     */
    Teacher findByUsername(@Param("username") String username);

    /**
     * 根据教师ID查询教师
     * @param teacherId 教师ID
     * @return 教师实体，未找到返回 null
     */
    Teacher selectById(@Param("teacherId") Long teacherId);

    /**
     * 新增教师
     * @param teacher 教师实体对象
     * @return 受影响的行数
     */
    int insert(Teacher teacher);

    /**
     * 更新教师信息
     * @param teacher 教师实体对象
     * @return 受影响的行数
     */
    int update(Teacher teacher);

    /**
     * 更新教师账号状态（启用/禁用）
     * @param teacherId 教师ID
     * @param status    账号状态（1=启用, 0=禁用）
     * @return 受影响的行数
     */
    int updateStatus(@Param("teacherId") Long teacherId, @Param("status") Integer status);

    /**
     * 修改教师密码
     * @param teacherId 教师ID
     * @param password  新密码（BCrypt加密后的密文）
     * @return 受影响的行数
     */
    int updatePassword(@Param("teacherId") Long teacherId, @Param("password") String password);

    /**
     * 根据关键字和状态分页查询教师列表（管理员用）
     * @param keyword 关键字（模糊匹配姓名或用户名）
     * @param status  账号状态（null 表示不限）
     * @param offset  偏移量
     * @param limit   每页条数
     * @return 教师列表
     */
    List<Teacher> findByKeyword(@Param("keyword") String keyword, @Param("status") Integer status,
                                @Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 统计符合条件的教师总数
     * @param keyword 关键字
     * @param status  账号状态
     * @return 教师总数
     */
    Long countByKeyword(@Param("keyword") String keyword, @Param("status") Integer status);

    /**
     * 根据教师ID删除教师（删除前需先清理课程关联，题目和试卷中的 teacher_id 无外键约束故不受影响）
     * @param teacherId 教师ID
     * @return 受影响的行数
     */
    int deleteById(@Param("teacherId") Long teacherId);
}
