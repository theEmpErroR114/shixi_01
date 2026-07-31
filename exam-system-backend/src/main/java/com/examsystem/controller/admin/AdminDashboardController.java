package com.examsystem.controller.admin;

import com.examsystem.dto.DashboardStatsVO;
import com.examsystem.dto.Result;
import com.examsystem.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员仪表盘控制器 — 提供后台首页的统计数据。
 * <p>
 * 统计数据包括：教师数量、学生数量、课程数量、题目数量、试卷数量等汇总指标，
 * 供 admin_dashboard.html 页面的 KPI 卡片展示。
 *
 * @see com.examsystem.service.AdminService#getDashboardStats() 统计数据查询
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private AdminService adminService;

    /**
     * 获取仪表盘统计数据。
     *
     * @return 包含各维度计数和统计信息的 VO 对象
     */
    @GetMapping("/stats")
    public Result<DashboardStatsVO> getStats() {
        return Result.success(adminService.getDashboardStats());
    }
}
