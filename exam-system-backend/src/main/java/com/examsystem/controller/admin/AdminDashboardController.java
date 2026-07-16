package com.examsystem.controller.admin;

import com.examsystem.dto.DashboardStatsVO;
import com.examsystem.dto.Result;
import com.examsystem.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/stats")
    public Result<DashboardStatsVO> getStats() {
        return Result.success(adminService.getDashboardStats());
    }
}
