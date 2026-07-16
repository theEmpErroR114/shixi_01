package com.examsystem.mapper;

import com.examsystem.entity.Student;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StudentMapper {

    Student findByUsername(@Param("username") String username);

    Student selectById(@Param("studentId") Long studentId);

    int insert(Student student);

    int update(Student student);

    int updateStatus(@Param("studentId") Long studentId, @Param("status") Integer status);

    int updatePassword(@Param("studentId") Long studentId, @Param("password") String password);

    List<Student> findByKeyword(@Param("keyword") String keyword, @Param("className") String className,
                                @Param("status") Integer status, @Param("offset") Integer offset,
                                @Param("limit") Integer limit);

    Long countByKeyword(@Param("keyword") String keyword, @Param("className") String className,
                        @Param("status") Integer status);

    List<java.util.Map<String, Object>> selectStudentStats(@Param("keyword") String keyword,
                                                            @Param("offset") Integer offset,
                                                            @Param("limit") Integer limit);

    List<java.util.Map<String, Object>> selectStudentStatsByStudentId(@Param("studentId") Long studentId);
}
