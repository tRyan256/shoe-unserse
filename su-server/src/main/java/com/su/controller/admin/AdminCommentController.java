package com.su.controller.admin;

import com.su.dto.CommentPageQueryDTO;
import com.su.result.PageResult;
import com.su.result.Result;
import com.su.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/comment")
@Slf4j
public class AdminCommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/page")
    public Result<PageResult> page(CommentPageQueryDTO dto) {
        return Result.success(commentService.page(dto));
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        commentService.deleteById(id);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        commentService.startOrStop(status, id);
        return Result.success();
    }
}

