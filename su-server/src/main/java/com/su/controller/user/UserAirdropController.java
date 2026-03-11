package com.su.controller.user;

import com.su.result.Result;
import com.su.service.AirdropService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/airdrop")
public class UserAirdropController {

    @Autowired
    private AirdropService airdropService;

    @PostMapping("/receive/{airdropId}")
    public Result<String> receive(@PathVariable Long airdropId) {
        airdropService.receive(airdropId);
        return Result.success();
    }
}
