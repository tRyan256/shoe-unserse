package com.su.controller.admin;

import com.su.entity.User;
import com.su.result.Result;
import com.su.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userMapper.getById(id));
    }
}

