package com.su.controller.user;

import com.su.context.BaseContext;
import com.su.result.PageResult;
import com.su.result.Result;
import com.su.service.UserAirdropHistoryService;
import com.su.vo.AirdropRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * User Airdrop History Controller
 * Handles airdrop participation history display
 */
@RestController
@RequestMapping("/user/profile/airdrops")
@Slf4j
public class UserAirdropHistoryController {

    @Autowired
    private UserAirdropHistoryService userAirdropHistoryService;

    /**
     * Get airdrop participation history for current user
     *
     * @param page Page number (default: 1)
     * @param size Page size (default: 20)
     * @return Paginated list of airdrop records
     */
    @GetMapping
    public Result<PageResult<AirdropRecordVO>> listAirdropRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long userId = BaseContext.getCurrentId();
        log.info("Getting airdrop records for user: {}, page: {}, size: {}", userId, page, size);

        PageResult pageResult = userAirdropHistoryService.listAirdropRecords(userId, page, size);
        return Result.success(pageResult);
    }
}
