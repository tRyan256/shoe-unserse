package com.su.mapper;

import com.github.pagehelper.Page;
import com.su.dto.CommentPageQueryDTO;
import com.su.entity.Comment;
import com.su.vo.CommentAdminVO;
import com.su.vo.CommentVO;
import com.su.vo.MyCommentVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    void insert(Comment comment);

    void deleteById(Long id);

    void updateStatus(Long id, Integer status);

    Page<Comment> pageQuery(CommentPageQueryDTO dto);

    Page<CommentAdminVO> pageQueryAdmin(CommentPageQueryDTO dto);

    List<CommentVO> listVOBySpuId(Long spuId);

    List<MyCommentVO> listMyComments(Long userId);
}
