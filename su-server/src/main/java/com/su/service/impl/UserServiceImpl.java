package com.su.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.su.constant.JwtClaimsConstant;
import com.su.constant.RedisKeyConstant;
import com.su.dto.UserLoginDTO;
import com.su.entity.User;
import com.su.exception.LoginFailedException;
import com.su.mapper.UserMapper;
import com.su.properties.JwtProperties;
import com.su.properties.LoginTestCodeProperties;
import com.su.result.Result;
import com.su.service.UserService;
import com.su.utils.JwtUtil;
import com.su.utils.RegexUtils;
import com.su.utils.cache.CacheClient;
import com.su.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CacheClient cacheClient;
    @Autowired
    private LoginTestCodeProperties loginTestCodeProperties;

    @Override
    public Result sendCode(String phone) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.error("手机号格式错误！");
        }

        String code = RandomUtil.randomNumbers(6);
        if (isFixedTestPhone(phone) && loginTestCodeProperties.getCode() != null && !loginTestCodeProperties.getCode().isBlank()) {
            code = loginTestCodeProperties.getCode();
            log.info("固定测试验证码已启用，phone={}", phone);
        }

        cacheClient.set(RedisKeyConstant.loginCodeKey(phone), code, Duration.ofMinutes(RedisKeyConstant.LOGIN_CODE_TTL));

        log.info("验证码: {}", code);

        return Result.success();
    }

    @Override
    public UserLoginVO login(UserLoginDTO loginForm) {
        String phone = loginForm.getPhone();

        if (RegexUtils.isPhoneInvalid(phone)) {
            throw new LoginFailedException("手机号格式错误！");
        }

        String code = loginForm.getCode();
        if (!isFixedTestCode(phone, code)) {
            String cacheCode = cacheClient.get(RedisKeyConstant.loginCodeKey(phone));
            if (cacheCode == null || !cacheCode.equals(code)) {
                throw new LoginFailedException("验证码不正确");
            }
            cacheClient.evict(RedisKeyConstant.loginCodeKey(phone));
        } else {
            log.info("使用固定测试验证码登录，phone={}", phone);
        }

        User user = userMapper.getByPhone(phone);
        if (user == null) {
            user = createUserWithPhone(phone);
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(
            jwtProperties.getUserSecretKey(),
            jwtProperties.getUserTtl(),
            claims
        );

        return UserLoginVO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .token(token)
                .build();
    }

    private boolean isFixedTestPhone(String phone) {
        return loginTestCodeProperties.isEnabled()
            && phone != null
            && phone.equals(loginTestCodeProperties.getPhone());
    }

    private boolean isFixedTestCode(String phone, String code) {
        return isFixedTestPhone(phone)
            && code != null
            && code.equals(loginTestCodeProperties.getCode());
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setName("用户" + RandomUtil.randomString(10));
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
        return user;
    }

    @Override
    public User getById(Long id) {
        return userMapper.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFollowerCount(Long userId, Integer increment) {
        if (userId == null || increment == null || increment == 0) {
            log.warn("updateFollowerCount: invalid parameters userId={}, increment={}", userId, increment);
            return;
        }
        log.info("updateFollowerCount: userId={}, increment={}", userId, increment);
        userMapper.updateFollowerCount(userId, increment);
        evictUserCache(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFollowingCount(Long userId, Integer increment) {
        if (userId == null || increment == null || increment == 0) {
            log.warn("updateFollowingCount: invalid parameters userId={}, increment={}", userId, increment);
            return;
        }
        log.info("updateFollowingCount: userId={}, increment={}", userId, increment);
        userMapper.updateFollowingCount(userId, increment);
        evictUserCache(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLikedCount(Long userId, Integer increment) {
        if (userId == null || increment == null || increment == 0) {
            log.warn("updateLikedCount: invalid parameters userId={}, increment={}", userId, increment);
            return;
        }
        log.info("updateLikedCount: userId={}, increment={}", userId, increment);
        userMapper.updateLikedCount(userId, increment);
        evictUserCache(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePostCount(Long userId, Integer increment) {
        if (userId == null || increment == null || increment == 0) {
            log.warn("updatePostCount: invalid parameters userId={}, increment={}", userId, increment);
            return;
        }
        log.info("updatePostCount: userId={}, increment={}", userId, increment);
        userMapper.updatePostCount(userId, increment);
        evictUserCache(userId);
    }

    private void evictUserCache(Long userId) {
        String key = RedisKeyConstant.userStatisticsKey(userId);
        cacheClient.evict(key);
        log.debug("evictUserCache: cleared cache for userId={}", userId);
    }
}
