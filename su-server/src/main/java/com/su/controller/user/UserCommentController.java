package com.su.controller.user;

import com.su.dto.CommentDTO;
import com.su.result.Result;
import com.su.service.CommentService;
import com.su.vo.CommentVO;
import com.su.vo.MyCommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/comment")
public class UserCommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping
    public Result<String> publish(@RequestBody CommentDTO dto) {
        commentService.publish(dto);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<CommentVO>> list(Long spuId) {
        return Result.success(commentService.listVOBySpuId(spuId));
    }

    @GetMapping("/my")
    public Result<List<MyCommentVO>> myComments() {
        return Result.success(commentService.listMyComments());
    }
}

