package com.su.controller.user;


import com.su.dto.UserLoginDTO;
import com.su.entity.User;
import com.su.result.Result;
import com.su.service.UserService;
import com.su.vo.UserLoginVO;
import com.su.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/sendCode")
    public Result sendCode(@RequestParam String phone) {
        log.info("发送验证码，手机号：{}", phone);
        return userService.sendCode(phone);
    }

    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录，手机号：{}", userLoginDTO.getPhone());
        UserLoginVO user = userService.login(userLoginDTO);
        return Result.success(user);
    }

    @GetMapping("/info")
    public Result<User> getUserInfo() {
        Long userId = BaseContext.getCurrentId();
        log.info("获取当前用户信息，userId：{}", userId);
        User user = userService.getById(userId);
        return Result.success(user);
    }
}
