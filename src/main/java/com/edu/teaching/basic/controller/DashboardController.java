package com.edu.teaching.basic.controller;

import cn.hutool.core.util.StrUtil;
import com.edu.teaching.common.Result;
import com.edu.teaching.basic.service.DashboardService;
import com.edu.teaching.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 首页数据统计：管理员返回全局数据，教师返回本人数据
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private JwtUtil<EduUserLoginController.LoginUser> jwtUtil;

    @GetMapping("/stats")
    public Result stats(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isEmpty(authorization)) {
            return Result.error(401, "未登录");
        }
        if (!jwtUtil.verifyToken(authorization)) {
            return Result.error(401, "登录已过期");
        }
        EduUserLoginController.LoginUser loginUser = jwtUtil.parseToken(authorization, EduUserLoginController.LoginUser.class);
        String role = loginUser.getRole() != null ? loginUser.getRole() : "";
        Long userId = loginUser.getId();
        Map<String, Long> stats = dashboardService.getStats(role, userId);
        return Result.success(stats);
    }

    /**
     * 管理员数据总览：总学生数、总教师数、课程数量、学习总人次、平均通过率（仅管理员）
     */
    @GetMapping("/overview")
    public Result overview(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isEmpty(authorization)) {
            return Result.error(401, "未登录");
        }
        if (!jwtUtil.verifyToken(authorization)) {
            return Result.error(401, "登录已过期");
        }
        EduUserLoginController.LoginUser loginUser = jwtUtil.parseToken(authorization, EduUserLoginController.LoginUser.class);
        String role = loginUser.getRole() != null ? loginUser.getRole() : "";
        if (!"admin".equalsIgnoreCase(role)) {
            return Result.error(403, "仅管理员可查看数据总览");
        }
        return Result.success(dashboardService.getAdminOverviewStats());
    }
}
