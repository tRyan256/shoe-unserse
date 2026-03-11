package com.su.service;

import com.su.dto.UserLoginDTO;
import com.su.entity.User;
import com.su.result.Result;
import com.su.vo.UserLoginVO;

public interface UserService {

    Result sendCode(String phone);

    UserLoginVO login(UserLoginDTO userLoginDTO);

    User getById(Long id);

    void updateFollowerCount(Long userId, Integer increment);

    void updateFollowingCount(Long userId, Integer increment);

    void updateLikedCount(Long userId, Integer increment);

    void updatePostCount(Long userId, Integer increment);
}
