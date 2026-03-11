package com.su.service;

import com.su.dto.CommentDTO;
import com.su.dto.CommentPageQueryDTO;
import com.su.result.PageResult;
import com.su.vo.CommentVO;
import com.su.vo.MyCommentVO;

import java.util.List;

public interface CommentService {

    /**
     * 发布评论
     * @param dto
     */
    void publish(CommentDTO dto);

    /**
     * 根据鞋款SPU id查询评论列表（带用户信息）
     * @param spuId
     * @return
     */
    List<CommentVO> listVOBySpuId(Long spuId);

    /**
     * 查询当前用户的评论列表
     * @return
     */
    List<MyCommentVO> listMyComments();

    /**
     * 分页查询评论
     * @param dto
     * @return
     */
    PageResult page(CommentPageQueryDTO dto);

    /**
     * 根据id删除评论
     * @param id
     */
    void deleteById(Long id);

    /**
     * 启用或禁用评论
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);
}
