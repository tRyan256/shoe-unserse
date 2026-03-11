package com.su.service.impl;

import com.su.dto.UserPersonalInfoUpdateDTO;
import com.su.entity.User;
import com.su.exception.ServiceException;
import com.su.mapper.UserMapper;
import com.su.service.UserPersonalInfoService;
import com.su.utils.RegexUtils;
import com.su.vo.UserPersonalInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User Personal Information Service Implementation
 */
@Service
@Slf4j
public class UserPersonalInfoServiceImpl implements UserPersonalInfoService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserPersonalInfoVO getPersonalInfo(Long userId) {
        log.info("Getting personal info for user: {}", userId);
        
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        return UserPersonalInfoVO.builder()
                .id(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .sex(user.getSex())
                .idNumber(user.getIdNumber())
                .avatar(user.getAvatar())
                .createTime(user.getCreateTime())
                .build();
    }

    @Override
    public void updatePersonalInfo(Long userId, UserPersonalInfoUpdateDTO dto) {
        log.info("Updating personal info for user: {}, data: {}", userId, dto);

        // Validate required fields
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ServiceException("姓名不能为空");
        }

        // Validate phone format if provided
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            if (RegexUtils.isPhoneInvalid(dto.getPhone())) {
                throw new ServiceException("手机号格式错误");
            }
        }

        // Validate ID number format if provided
        if (dto.getIdNumber() != null && !dto.getIdNumber().isEmpty()) {
            if (RegexUtils.isIdNumberInvalid(dto.getIdNumber())) {
                throw new ServiceException("身份证号格式错误");
            }
        }

        // Check if user exists
        User existingUser = userMapper.getById(userId);
        if (existingUser == null) {
            throw new ServiceException("用户不存在");
        }

        // Update user
        User user = new User();
        user.setId(userId);
        BeanUtils.copyProperties(dto, user);
        
        userMapper.update(user);
        
        log.info("Personal info updated successfully for user: {}", userId);
    }
}
