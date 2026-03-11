package com.su.service;

import com.su.dto.UserPersonalInfoUpdateDTO;
import com.su.vo.UserPersonalInfoVO;

/**
 * User Personal Information Service
 */
public interface UserPersonalInfoService {

    /**
     * Get personal information for a user
     * @param userId User ID
     * @return User personal information
     */
    UserPersonalInfoVO getPersonalInfo(Long userId);

    /**
     * Update personal information for a user
     * @param userId User ID
     * @param dto Update data
     */
    void updatePersonalInfo(Long userId, UserPersonalInfoUpdateDTO dto);
}
