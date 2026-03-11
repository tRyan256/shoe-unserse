package com.su.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.su.context.BaseContext;
import com.su.dto.CommentDTO;
import com.su.dto.CommentPageQueryDTO;
import com.su.entity.Comment;
import com.su.entity.Orders;
import com.su.exception.OrderBusinessException;
import com.su.mapper.CommentMapper;
import com.su.mapper.OrderMapper;
import com.su.result.PageResult;
import com.su.service.CommentService;
import com.su.vo.CommentAdminVO;
import com.su.vo.CommentVO;
import com.su.vo.MyCommentVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private OrderMapper orderMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void publish(CommentDTO dto) {
        Long userId = BaseContext.getCurrentId();
        Long spuId = dto.getSpuId();

        Integer signedCount = orderMapper.countSignedOrderByUserAndSpu(userId, spuId);
        if (signedCount == null || signedCount == 0) {
            throw new OrderBusinessException("只有购买并签收该商品后才能评论");
        }

        if (dto.getRating() != null && (dto.getRating() < 1 || dto.getRating() > 5)) {
            throw new OrderBusinessException("评分不合法");
        }

        Comment comment = new Comment();
        BeanUtils.copyProperties(dto, comment);
        comment.setUserId(userId);
        comment.setStatus(1);
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);

        Long orderId = dto.getOrderId();
        String orderNumber = dto.getOrderNumber();
        if (orderId == null && orderNumber != null && !orderNumber.isBlank()) {
            Orders orders = orderMapper.getByNumber(orderNumber);
            if (orders == null || orders.getUserId() == null || !orders.getUserId().equals(userId)) {
                throw new OrderBusinessException("订单不存在");
            }
            orderId = orders.getId();
        }
        if (orderId != null) {
            int updated = orderMapper.markReviewedIfEligible(orderId, userId);
            if (updated == 0) {
                throw new OrderBusinessException("订单状态已变化，请刷新后重试");
            }
        }
    }

    @Override
    public List<CommentVO> listVOBySpuId(Long spuId) {
        return commentMapper.listVOBySpuId(spuId);
    }

    @Override
    public List<MyCommentVO> listMyComments() {
        Long userId = BaseContext.getCurrentId();
        return commentMapper.listMyComments(userId);
    }

    @Override
    public PageResult page(CommentPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<CommentAdminVO> page = commentMapper.pageQueryAdmin(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void deleteById(Long id) {
        commentMapper.deleteById(id);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        commentMapper.updateStatus(id, status);
    }
}
