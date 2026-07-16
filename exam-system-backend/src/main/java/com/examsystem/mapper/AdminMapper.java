package com.examsystem.mapper;

import com.examsystem.entity.Admin;
import org.apache.ibatis.annotations.Param;

public interface AdminMapper {

    Admin findByUsername(@Param("username") String username);

    Admin selectById(@Param("adminId") Long adminId);

    int insert(Admin admin);

    int update(Admin admin);
}
