package com.example.springboot.controller;

import com.example.springboot.entity.User;
import com.example.springboot.req.LoginReq;
import com.example.springboot.service.UserService;
import com.example.springboot.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.springboot.utils.RedisUtil;
import com.example.springboot.utils.CommonConstant;
import java.time.format.DateTimeFormatter;


import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
@Tag(name = "UserController", description = "用户账户相关接口")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RedisUtil redisUtil;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.base-url}")
    private String baseUrl;

    @Operation(summary = "上传头像")
    @PostMapping("/avatars")
    public Map<String, Object> uploadAvatar(@RequestParam("file") MultipartFile file,
                                            @RequestParam(value = "userId", required = false) Integer userId) {

        Map<String, Object> response = new HashMap<>();

        try {

            if (file.isEmpty()) {
                response.put("code", 400);
                response.put("msg", "文件不能为空");
                return response;
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("code", 400);
                response.put("msg", "只支持图片文件");
                return response;
            }

            //文件大小验证
            if (file.getSize() > 5 * 1024 * 1024) {
                response.put("code", 400);
                response.put("msg", "文件大小不能超过5MB");
                return response;
            }

            //生成文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String fileName = UUID.randomUUID().toString() + fileExtension;

            //创建目录
            File avatarDir = new File(uploadDir + "avatar/");
            if (!avatarDir.exists()) {
                avatarDir.mkdirs();
            }

            //保存文件
            File dest = new File(avatarDir, fileName);
            file.transferTo(dest);

            //生成访问URL
            String url = baseUrl + "/uploads/avatar/" + fileName;

            if (userId != null && userId > 0) {
                //更新用户头像
                int updateResult = userService.updateAvatar(userId, url, LocalDateTime.now());

                if (updateResult > 0) {
                    response.put("code", 200);
                    response.put("msg", "头像上传成功");
                    response.put("url", url);
                } else {
                    response.put("code", 500);
                    response.put("msg", "头像更新失败，用户可能不存在");
                }
            } else {
                response.put("code", 200);
                response.put("msg", "头像上传成功");
                response.put("url", url);
            }

        } catch (Exception e) {
            response.put("code", 500);
            response.put("msg", "头像上传异常: " + e.getMessage());
        }

        return response;
    }

    //用户注册
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Map<String, String> insert(@RequestBody User user) {
        Map<String, String> res = new HashMap<>();

        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            User existingUserByUsername = userService.selectByUsername(user.getUsername());
            if (existingUserByUsername != null) {
                res.put("code", "400");
                res.put("msg", "该用户名已被使用");
                return res;
            }
        }

        //检查手机号是否已存在
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            User existingUserByPhone = userService.selectByPhone(user.getPhone());
            if (existingUserByPhone != null) {
                res.put("code", "400");
                res.put("msg", "该手机号已注册");
                return res;
            }
        }

        //检查邮箱是否已存在
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            User existingUserByEmail = userService.selectByEmail(user.getEmail());
            if (existingUserByEmail != null) {
                res.put("code", "400");
                res.put("msg", "该邮箱已注册");
                return res;
            }
        }

        //设置注册时间和其他默认值
        LocalDateTime now = LocalDateTime.now();
        user.setRegisterTime(now);
        user.setUpdateTime(now);
        user.setRole("user");
        if (user.getAvatarUrl() == null || user.getAvatarUrl().trim().isEmpty()) {
            user.setAvatarUrl("");
        }

        //执行注册
        int result = userService.insert(user);
        if (result > 0) {
            res.put("code", "200");
            res.put("msg", "注册成功");
        } else {
            res.put("code", "500");
            res.put("msg", "注册失败");
        }

        return res;
    }

    //登录
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginReq req) {
        String username = req.getUsername();
        String password = req.getPassword();


        //查询用户
        User user = userService.selectByUsername(username);
        Map<String, Object> res = new HashMap<>();

        //校验逻辑
        if (user == null) {
            res.put("code", 400);
            res.put("msg", "账号不存在");
            return res;
        }

        if (!user.getPassword().equals(password)) {
            res.put("code", 401);
            res.put("msg", "密码错误");
            return res;
        }

        //登录成功
        try {
            userService.update(user);

            String token = JwtUtils.generateToken(user.getUserId());

            redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + user.getUsername(), token, JwtUtils.EXPIRE_TIME_SECONDS);

            user.setPassword(null);
            user.setUpdateTime(user.getUpdateTime());

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("userInfo", user);

            res.put("code", 200);
            res.put("msg", "登录成功");
            res.put("data", data);

        } catch (Exception e) {
            res.put("code", 500);
            res.put("msg", "登录异常：" + e.getMessage());
        }
        return res;
    }

    //查询用户
    @Operation(summary = "查询特定用户")
    @GetMapping("/{id}")
    public User selectById(@PathVariable Integer id) {
        return userService.selectById(id);
    }

    //查询所有用户
    @Operation(summary = "查询所有用户")
    @GetMapping
    public List<User> selectAll() {
        return userService.selectAll();
    }

    //更新用户信息
    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public String update(@PathVariable Integer id, @RequestBody User user) {
        user.setUserId(id);
        user.setUpdateTime(LocalDateTime.now());
        int result = userService.update(user);
        return result > 0 ? "更新成功" : "更新失败";
    }

    //更新密码
    @Operation(summary = "更新用户密码")
    @PutMapping("/{id}/password")
    public String updatePassword(@PathVariable Integer id, @RequestParam String password) {
        LocalDateTime now = LocalDateTime.now();
        int result = userService.updatePassword(id, password, now);
        return result > 0 ? "密码更新成功" : "密码更新失败";
    }

    //删除用户
    @Operation(summary = "删除用户信息")
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        int result = userService.delete(id);
        return result > 0 ? "删除成功" : "删除失败";
    }

    //更新最后登录时间
    @Operation(summary = "更新最后登录时间")
    @PutMapping("/{id}/last-login-time")
    public Map<String, Object> updateLastLoginTime(@PathVariable("id") Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean result = userService.updateLastLoginTime(id, LocalDateTime.now());
            if (result) {
                response.put("code", 200);
                response.put("msg", "最后登录时间更新成功");
            } else {
                response.put("code", 400);
                response.put("msg", "更新失败，用户不存在");
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("msg", "更新最后登录时间失败: " + e.getMessage());
        }
        return response;
    }
    //设置下次体质测试提醒
    @Operation(summary = "设置下次体质测试提醒")
    @PutMapping("/{userId}/test-reminder")
    public Map<String, Object> setReminder(@PathVariable("userId") Long userId, @RequestBody Map<String, String> params) {
        Map<String, Object> res = new HashMap<>();
        try {
            String timeStr = params.get("time");
            if (timeStr == null) {
                res.put("code", 400);
                res.put("msg", "请选择时间");
                return res;
            }

            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime nextTime = LocalDateTime.parse(timeStr, df);

            boolean success = userService.setReminder(userId.intValue(), nextTime);

            if (success) {
                res.put("code", 200);
                res.put("msg", "提醒设置成功");
            } else {
                res.put("code", 400);
                res.put("msg", "设置失败");
            }
        } catch (Exception e) {
            res.put("code", 500);
            res.put("msg", "系统异常：" + e.getMessage());
        }
        return res;
    }

    @Operation(summary = "增加用户积分", description = "前端判断连签满7天后调用，传入1000")
    @PostMapping("/{userId}/points")
    public int addPoints(@PathVariable Integer userId, @RequestParam Integer amount) {
        return userService.addPoints(userId, amount);
    }

    //保存用户选择的健康偏好
    @Operation(summary = "保存用户选择的健康偏好")
    @PutMapping("/{userId}/preferences")
    public Map<String, Object> savePreferences(@PathVariable Integer userId, @RequestBody Map<String, Object> params) {
        Map<String, Object> res = new HashMap<>();
        try {
            String content = (String) params.get("content");

            if (userId == null || content == null || content.trim().isEmpty()) {
                res.put("code", 400);
                res.put("msg", "参数不能为空");
                return res;
            }
            int result = userService.updateContent(userId, content);
            if (result > 0) {
                res.put("code", 200);
                res.put("msg", "保存成功");
            } else {
                res.put("code", 400);
                res.put("msg", "保存失败");
            }
        } catch (Exception e) {
            res.put("code", 500);
            res.put("msg", "系统异常：" + e.getMessage());
        }
        return res;
    }
    //获取当前用户信息
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/{userId}/profile")
    public Map<String, Object> getCurrentUser(@PathVariable Integer userId) {
        Map<String, Object> res = new HashMap<>();
        try {
            User user = userService.selectById(userId);
            if (user != null) {
                user.setPassword(null);
                res.put("code", 200);
                res.put("msg", "获取成功");
                res.put("data", user);
            } else {
                res.put("code", 404);
                res.put("msg", "用户不存在");
            }
        } catch (Exception e) {
            res.put("code", 500);
            res.put("msg", "系统异常：" + e.getMessage());
        }
        return res;
    }
}
