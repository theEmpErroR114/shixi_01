package com.examsystem.mapper;

import com.examsystem.entity.Teacher;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TeacherMapper {

    Teacher findByUsername(@Param("username") String username);

    Teacher selectById(@Param("teacherId") Long teacherId);

    int insert(Teacher teacher);

    int update(Teacher teacher);

    int updateStatus(@Param("teacherId") Long teacherId, @Param("status") Integer status);

    int updatePassword(@Param("teacherId") Long teacherId, @Param("password") String password);

    List<Teacher> findByKeyword(@Param("keyword") String keyword, @Param("status") Integer status,
                                @Param("offset") Integer offset, @Param("limit") Integer limit);

    Long countByKeyword(@Param("keyword") String keyword, @Param("status") Integer status);
}
