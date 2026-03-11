package com.su.controller.user;

import com.su.dto.DrawWinConfirmDTO;
import com.su.entity.Draw;
import com.su.entity.DrawRecord;
import com.su.result.Result;
import com.su.service.DrawService;
import com.su.vo.DrawDetailVO;
import com.su.vo.DrawWinOptionsVO;
import com.su.vo.DrawWinnerPublicVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/draw")
public class UserDrawController {

    @Autowired
    private DrawService drawService;

    @GetMapping("/list")
    public Result<List<Draw>> list() {
        return Result.success(drawService.listActive());
    }

    @PostMapping("/join")
    public Result<String> join(@RequestBody(required = false) Map<String, Long> body) {
        Long drawId = body == null ? null : body.get("drawId");
        drawService.join(drawId);
        return Result.success();
    }

    @GetMapping("/result/{drawId}")
    public Result<DrawRecord> result(@PathVariable Long drawId) {
        return Result.success(drawService.myResult(drawId));
    }

    @GetMapping("/detail/{drawId}")
    public Result<DrawDetailVO> detail(@PathVariable Long drawId) {
        return Result.success(drawService.detail(drawId));
    }

    @GetMapping("/win/{drawId}/options")
    public Result<DrawWinOptionsVO> winOptions(@PathVariable Long drawId) {
        return Result.success(drawService.winOptions(drawId));
    }

    @PostMapping("/win/{drawId}/confirm")
    public Result<String> winConfirm(@PathVariable Long drawId, @RequestBody DrawWinConfirmDTO dto) {
        return Result.success(drawService.winConfirm(drawId, dto));
    }

    @PostMapping("/win/{drawId}/giveUp")
    public Result<String> giveUp(@PathVariable Long drawId) {
        drawService.giveUp(drawId);
        return Result.success();
    }

    @GetMapping("/winners/{drawId}")
    public Result<List<DrawWinnerPublicVO>> winners(@PathVariable Long drawId) {
        return Result.success(drawService.winnersPublic(drawId));
    }
}