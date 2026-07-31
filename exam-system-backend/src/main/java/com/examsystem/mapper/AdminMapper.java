package com.examsystem.mapper;

import com.examsystem.entity.Admin;
import org.apache.ibatis.annotations.Param;

/**
 * 管理员 Mapper 接口
 * 定义对 t_admin 表的数据库操作
 */
public interface AdminMapper {

    /**
     * 根据用户名查询管理员
     * @param username 管理员用户名
     * @return 管理员实体，未找到返回 null
     */
    Admin findByUsername(@Param("username") String username);

    /**
     * 根据管理员ID查询管理员
     * @param adminId 管理员ID
     * @return 管理员实体，未找到返回 null
     */
    Admin selectById(@Param("adminId") Long adminId);

    /**
     * 新增管理员
     * @param admin 管理员实体对象
     * @return 受影响的行数
     */
    int insert(Admin admin);

    /**
     * 更新管理员信息
     * @param admin 管理员实体对象
     * @return 受影响的行数
     */
    int update(Admin admin);

    /**
     * 修改管理员密码
     * @param adminId  管理员ID
     * @param password 新密码（BCrypt加密后的密文）
     * @return 受影响的行数
     */
    int updatePassword(@Param("adminId") Long adminId, @Param("password") String password);
}
