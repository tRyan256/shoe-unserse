package com.su.controller.user;

import com.su.context.BaseContext;
import com.su.dto.UserPersonalInfoUpdateDTO;
import com.su.result.Result;
import com.su.service.UserPersonalInfoService;
import com.su.vo.UserPersonalInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * User Personal Information Controller
 * Handles personal information view and edit operations
 */
@RestController
@RequestMapping("/user/profile/personal")
@Slf4j
public class UserPersonalInfoController {

    @Autowired
    private UserPersonalInfoService userPersonalInfoService;

    /**
     * Get current user's personal information
     *
     * @return User personal information
     */
    @GetMapping
    public Result<UserPersonalInfoVO> getPersonalInfo() {
        Long userId = BaseContext.getCurrentId();
        log.info("Getting personal info for user: {}", userId);

        UserPersonalInfoVO vo = userPersonalInfoService.getPersonalInfo(userId);
        return Result.success(vo);
    }

    /**
     * Update current user's personal information
     *
     * @param dto Update data
     * @return Success result
     */
    @PutMapping
    public Result<Void> updatePersonalInfo(@RequestBody UserPersonalInfoUpdateDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("Updating personal info for user: {}, data: {}", userId, dto);

        userPersonalInfoService.updatePersonalInfo(userId, dto);
        return Result.success();
    }
}
