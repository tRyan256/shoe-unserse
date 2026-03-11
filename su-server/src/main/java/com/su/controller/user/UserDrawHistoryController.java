package com.su.controller.user;

import com.su.context.BaseContext;
import com.su.result.PageResult;
import com.su.result.Result;
import com.su.service.UserDrawHistoryService;
import com.su.vo.DrawRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * User Draw History Controller
 * Handles draw participation history display
 */
@RestController
@RequestMapping("/user/profile/draws")
@Slf4j
public class UserDrawHistoryController {

    @Autowired
    private UserDrawHistoryService userDrawHistoryService;

    /**
     * Get draw participation history for current user
     *
     * @param page Page number (default: 1)
     * @param size Page size (default: 20)
     * @return Paginated list of draw records
     */
    @GetMapping
    public Result<PageResult<DrawRecordVO>> listDrawRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long userId = BaseContext.getCurrentId();
        log.info("Getting draw records for user: {}, page: {}, size: {}", userId, page, size);

        PageResult pageResult = userDrawHistoryService.listDrawRecords(userId, page, size);
        return Result.success(pageResult);
    }
}
