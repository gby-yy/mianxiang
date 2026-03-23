package com.edu.teaching.basic.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.teaching.common.Result;
import com.edu.teaching.module.edu_student_user.entity.EduStudentUser;
import com.edu.teaching.module.edu_student_user.service.EduStudentUserService;
import com.edu.teaching.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 学生端登录、注册、信息、登出
 * 与管理端 /edu_user/login 隔离，使用 /student 前缀
 */
@RestController
@RequestMapping("/student")
public class StudentAuthController {

    /** 学生 token 缓存：token -> 时间戳，用于校验与登出 */
    private static final Map<String, Long> STUDENT_TOKEN_CACHE_MAP = new HashMap<>();

    /** 供其他学生端接口校验 token 并获取学生 ID 使用 */
    public static Map<String, Long> getStudentTokenCacheMap() {
        return STUDENT_TOKEN_CACHE_MAP;
    }

    @Autowired
    private EduStudentUserService eduStudentUserService;

    @Autowired
    private JwtUtil<LoginUser> jwtUtil;

    /** 与前端一致的密码盐值 */
    private static final String PASSWORD_SALT = "intelligent_teaching_system";

    /**
     * 学生登录
     * 前端传入密码为 SHA256(salt + 明文)；数据库存明文，此处用相同盐做 SHA256 后比较
     */
    @PostMapping("/login")
    public Result login(@RequestBody StudentLoginVO vo) {
        if (StrUtil.isBlank(vo.getUsername())) {
            return Result.error("登录账号不能为空");
        }
        if (StrUtil.isBlank(vo.getPassword())) {
            return Result.error("登录密码不能为空");
        }
        EduStudentUser user = eduStudentUserService.getOne(
            new LambdaQueryWrapper<EduStudentUser>().eq(EduStudentUser::getUsername, vo.getUsername().trim()));
        if (user == null) {
            return Result.error("账号不存在");
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            return Result.error("账号已停用");
        }
        String inputHash = vo.getPassword();
        String dbPassword = user.getPassword() != null ? user.getPassword() : "";
        // 数据库可能存明文或历史存了密文：64 位十六进制视为密文直接比较，否则按明文用盐做 SHA256 后比较
        boolean passwordMatch = false;
        if (dbPassword.length() == 64 && dbPassword.matches("^[a-fA-F0-9]+$")) {
            passwordMatch = inputHash.equals(dbPassword);
        } else {
            passwordMatch = inputHash.equals(sha256(PASSWORD_SALT + dbPassword));
        }
        if (!passwordMatch) {
            return Result.error("密码错误");
        }
        LoginUser loginUser = new LoginUser();
        loginUser.setId(user.getId());
        loginUser.setName(StrUtil.isNotBlank(user.getRealName()) ? user.getRealName() : user.getUsername());
        loginUser.setAvatar(StrUtil.isNotBlank(user.getAvatar()) ? user.getAvatar() : "");
        loginUser.setRole("student");
        String token = jwtUtil.createToken(loginUser);
        STUDENT_TOKEN_CACHE_MAP.put(token, System.currentTimeMillis());
        return Result.success(token);
    }

    /**
     * 学生注册
     * 前端传入 password 为 SHA256("intelligent_teaching_system" + 明文)，直接入库
     */
    @PostMapping("/register")
    public Result register(@RequestBody StudentRegisterVO vo) {
        if (StrUtil.isBlank(vo.getUsername())) {
            return Result.error("登录账号不能为空");
        }
        if (StrUtil.isBlank(vo.getPassword())) {
            return Result.error("密码不能为空");
        }
        if (vo.getUsername().trim().length() < 2 || vo.getUsername().trim().length() > 32) {
            return Result.error("登录账号长度需在 2~32 个字符");
        }
        if (vo.getPassword().length() < 6) {
            return Result.error("密码不少于6位");
        }
        long count = eduStudentUserService.count(
            new LambdaQueryWrapper<EduStudentUser>().eq(EduStudentUser::getUsername, vo.getUsername().trim()));
        if (count > 0) {
            return Result.error("该账号已被注册");
        }
        EduStudentUser entity = new EduStudentUser();
        entity.setUsername(vo.getUsername().trim());
        entity.setPassword(vo.getPassword());
        entity.setRealName(StrUtil.isNotBlank(vo.getRealName()) ? vo.getRealName().trim() : null);
        entity.setPhone(StrUtil.isNotBlank(vo.getPhone()) ? vo.getPhone().trim() : null);
        entity.setEmail(StrUtil.isNotBlank(vo.getEmail()) ? vo.getEmail().trim() : null);
        entity.setGrade(StrUtil.isNotBlank(vo.getGrade()) ? vo.getGrade().trim() : null);
        entity.setMajor(StrUtil.isNotBlank(vo.getMajor()) ? vo.getMajor().trim() : null);
        entity.setIntro(StrUtil.isNotBlank(vo.getIntro()) ? vo.getIntro().trim() : null);
        entity.setStatus(1);
        entity.setCreateTime(LocalDateTime.now());
        eduStudentUserService.save(entity);
        return Result.success("注册成功");
    }

    /**
     * 获取当前学生信息（需携带学生 token）
     */
    @GetMapping("/info")
    public Result info(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isBlank(authorization)) {
            return Result.error(401, "尚未登录");
        }
        if (STUDENT_TOKEN_CACHE_MAP.get(authorization) == null) {
            return Result.error(401, "登录已过期");
        }
        if (!jwtUtil.verifyToken(authorization)) {
            return Result.error(401, "登录已过期");
        }
        LoginUser loginUser = jwtUtil.parseToken(authorization, LoginUser.class);
        EduStudentUser user = eduStudentUserService.getById(loginUser.getId());
        if (user == null) {
            return Result.error(401, "用户不存在");
        }
        LoginUser info = new LoginUser();
        info.setId(user.getId());
        info.setName(StrUtil.isNotBlank(user.getRealName()) ? user.getRealName() : user.getUsername());
        info.setAvatar(StrUtil.isNotBlank(user.getAvatar()) ? user.getAvatar() : "");
        info.setRole("student");
        return Result.success(info);
    }

    /**
     * 学生登出
     */
    @GetMapping("/logout")
    public Result logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(token)) {
            STUDENT_TOKEN_CACHE_MAP.remove(token);
        }
        return Result.success("退出成功");
    }

    /**
     * 获取当前学生完整资料（个人中心展示与编辑用，不含密码）
     */
    @GetMapping("/profile")
    public Result profile(HttpServletRequest request) {
        Long studentId = getStudentId(request);
        if (studentId == null) {
            return Result.error(401, "尚未登录");
        }
        EduStudentUser user = eduStudentUserService.getById(studentId);
        if (user == null) {
            return Result.error(401, "用户不存在");
        }
        StudentProfileVO vo = new StudentProfileVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setGrade(user.getGrade());
        vo.setMajor(user.getMajor());
        vo.setIntro(user.getIntro());
        return Result.success(vo);
    }

    /**
     * 更新当前学生资料（不修改账号、密码）
     */
    @PutMapping("/profile")
    public Result updateProfile(HttpServletRequest request, @RequestBody StudentProfileVO vo) {
        Long studentId = getStudentId(request);
        if (studentId == null) {
            return Result.error(401, "尚未登录");
        }
        EduStudentUser user = eduStudentUserService.getById(studentId);
        if (user == null) {
            return Result.error(401, "用户不存在");
        }
        if (StrUtil.isNotBlank(vo.getRealName())) {
            user.setRealName(vo.getRealName().trim());
        } else {
            user.setRealName(null);
        }
        user.setPhone(StrUtil.isNotBlank(vo.getPhone()) ? vo.getPhone().trim() : null);
        user.setEmail(StrUtil.isNotBlank(vo.getEmail()) ? vo.getEmail().trim() : null);
        user.setAvatar(StrUtil.isNotBlank(vo.getAvatar()) ? vo.getAvatar().trim() : null);
        user.setGrade(StrUtil.isNotBlank(vo.getGrade()) ? vo.getGrade().trim() : null);
        user.setMajor(StrUtil.isNotBlank(vo.getMajor()) ? vo.getMajor().trim() : null);
        user.setIntro(StrUtil.isNotBlank(vo.getIntro()) ? vo.getIntro().trim() : null);
        eduStudentUserService.updateById(user);
        return Result.success("保存成功");
    }

    /**
     * 修改密码。前端传入 oldPassword、newPassword 均为 SHA256(salt + 明文)
     */
    @PostMapping("/password")
    public Result changePassword(HttpServletRequest request, @RequestBody StudentPasswordVO vo) {
        Long studentId = getStudentId(request);
        if (studentId == null) {
            return Result.error(401, "尚未登录");
        }
        if (StrUtil.isBlank(vo.getOldPassword())) {
            return Result.error("请输入当前密码");
        }
        if (StrUtil.isBlank(vo.getNewPassword()) || vo.getNewPassword().length() < 6) {
            return Result.error("新密码不少于6位");
        }
        EduStudentUser user = eduStudentUserService.getById(studentId);
        if (user == null) {
            return Result.error(401, "用户不存在");
        }
        String dbPassword = user.getPassword() != null ? user.getPassword() : "";
        boolean oldMatch = false;
        if (dbPassword.length() == 64 && dbPassword.matches("^[a-fA-F0-9]+$")) {
            oldMatch = vo.getOldPassword().equals(dbPassword);
        } else {
            oldMatch = vo.getOldPassword().equals(sha256(PASSWORD_SALT + dbPassword));
        }
        if (!oldMatch) {
            return Result.error("当前密码错误");
        }
        user.setPassword(vo.getNewPassword());
        eduStudentUserService.updateById(user);
        return Result.success("密码已修改，请重新登录");
    }

    private Long getStudentId(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isBlank(authorization) || STUDENT_TOKEN_CACHE_MAP.get(authorization) == null) {
            return null;
        }
        if (!jwtUtil.verifyToken(authorization)) {
            return null;
        }
        LoginUser loginUser = jwtUtil.parseToken(authorization, LoginUser.class);
        return loginUser != null ? loginUser.getId() : null;
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    public static class StudentLoginVO {
        private String username;
        private String password;
    }

    @Data
    public static class StudentRegisterVO {
        private String username;
        private String password;
        private String realName;
        private String phone;
        private String email;
        private String grade;
        private String major;
        private String intro;
    }

    @Data
    public static class LoginUser {
        private Long id;
        private String name;
        private String avatar;
        private String role;
    }

    /** 个人中心资料（不含密码） */
    @Data
    public static class StudentProfileVO {
        private Long id;
        private String username;
        private String realName;
        private String phone;
        private String email;
        private String avatar;
        private String grade;
        private String major;
        private String intro;
    }

    @Data
    public static class StudentPasswordVO {
        private String oldPassword;
        private String newPassword;
    }
}

