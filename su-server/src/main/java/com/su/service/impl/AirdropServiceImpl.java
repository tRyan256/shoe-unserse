package com.su.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.su.component.delay.DelayQueueService;
import com.su.constant.DelayTaskTypeConstant;
import com.su.constant.RabbitMqConstant;
import com.su.constant.RedisKeyConstant;
import com.su.context.BaseContext;
import com.su.dto.AirdropDTO;
import com.su.dto.AirdropPageQueryDTO;
import com.su.entity.Airdrop;
import com.su.entity.Coupon;
import com.su.exception.OrderBusinessException;
import com.su.mapper.AirdropMapper;
import com.su.mapper.AirdropRecordMapper;
import com.su.mapper.CouponMapper;
import com.su.mq.message.AirdropClaimMessage;
import com.su.result.PageResult;
import com.su.service.AirdropService;
import com.su.service.airdrop.support.AirdropMeta;
import com.su.service.airdrop.support.AirdropRedisService;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

@Service
public class AirdropServiceImpl implements AirdropService {

    @Autowired
    private AirdropMapper airdropMapper;
    @Autowired
    private AirdropRecordMapper airdropRecordMapper;
    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private AirdropRedisService airdropRedisService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DelayQueueService delayQueueService;

    @Override
    public void save(AirdropDTO dto) {
        Airdrop airdrop = new Airdrop();
        BeanUtils.copyProperties(dto, airdrop);
        LocalDateTime now = LocalDateTime.now();
        if (airdrop.getRemainCount() == null) {
            airdrop.setRemainCount(airdrop.getTotalCount());
        }
        if (airdrop.getStatus() == null) {
            airdrop.setStatus(0);
        }
        if (airdrop.getStatus() != null && airdrop.getStatus() == 0
                && airdrop.getStartTime() != null
                && !airdrop.getStartTime().isAfter(now)) {
            airdrop.setStatus(1);
        }
        airdrop.setCreateTime(now);
        airdrop.setUpdateTime(now);
        airdropMapper.insert(airdrop);

        Coupon coupon = couponMapper.getById(airdrop.getCouponId());
        airdropRedisService.warmupAirdropMeta(airdrop, coupon);
        airdropRedisService.initIfAbsent(airdrop);
        scheduleStartIfNeeded(airdrop);
        scheduleEndIfNeeded(airdrop);
    }

    @Override
    public PageResult page(AirdropPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<Airdrop> page = airdropMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public Airdrop getById(Long id) {
        return airdropMapper.getById(id);
    }

    @Override
    public void update(AirdropDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new OrderBusinessException("参数错误");
        }
        Airdrop existing = airdropMapper.getById(dto.getId());
        if (existing == null) {
            throw new OrderBusinessException("空投活动不存在");
        }
        throw new OrderBusinessException("空投活动创建后不支持编辑，请取消后重新创建");
    }

    @Override
    public void deleteById(Long id) {
        airdropMapper.deleteById(id);
        airdropRedisService.delete(id);
    }

    @Override
    public void cancel(Long airdropId) {
        if (airdropId == null) {
            throw new OrderBusinessException("参数错误");
        }
        Airdrop airdrop = airdropMapper.getById(airdropId);
        if (airdrop == null) {
            throw new OrderBusinessException("空投活动不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (airdrop.getStatus() == null || airdrop.getStatus() != 0) {
            throw new OrderBusinessException("空投活动已开始，无法取消");
        }
        if (airdrop.getStartTime() != null && !airdrop.getStartTime().isAfter(now)) {
            throw new OrderBusinessException("空投活动已开始，无法取消");
        }
        int claimed = airdropRecordMapper.countByAirdropId(airdropId);
        if (claimed > 0) {
            throw new OrderBusinessException("空投活动已有领取记录，无法取消");
        }
        airdropRedisService.delete(airdropId);
        airdropMapper.deleteById(airdropId);
    }

    @Override
    public void receive(Long airdropId) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new OrderBusinessException("用户未登录");
        }
        LocalDateTime now = LocalDateTime.now();
        AirdropMeta airdropMeta = airdropRedisService.getMeta(airdropId);
        if (airdropMeta == null) {
            throw new OrderBusinessException("空投活动缓存未就绪，请稍后重试");
        }
        if (airdropMeta.getStatus() == null || airdropMeta.getStatus() != 1) {
            throw new OrderBusinessException("空投活动未开始");
        }
        if (airdropMeta.getStartTime() != null && now.isBefore(airdropMeta.getStartTime())) {
            throw new OrderBusinessException("空投活动未开始");
        }
        if (airdropMeta.getEndTime() != null && now.isAfter(airdropMeta.getEndTime())) {
            throw new OrderBusinessException("空投活动已结束");
        }
        var couponMeta = airdropMeta.getCoupon();
        if (couponMeta == null) {
            throw new OrderBusinessException("优惠券不可用");
        }
        if (couponMeta.getStatus() == null || couponMeta.getStatus() != 1) {
            throw new OrderBusinessException("优惠券不可用");
        }
        if (couponMeta.getStartTime() != null && now.isBefore(couponMeta.getStartTime())) {
            throw new OrderBusinessException("优惠券未开始");
        }
        if (couponMeta.getEndTime() != null && now.isAfter(couponMeta.getEndTime())) {
            throw new OrderBusinessException("优惠券已过期");
        }

        long claim = airdropRedisService.claim(airdropId, userId, airdropMeta.getRemainCount(), airdropMeta.getEndTime());
        if (claim == AirdropRedisService.ALREADY_CLAIMED) {
            return;
        }
        if (claim == AirdropRedisService.OUT_OF_STOCK) {
            throw new OrderBusinessException("空投已领完");
        }
        if (claim == AirdropRedisService.CACHE_NOT_READY || claim == AirdropRedisService.INVALID_ARGUMENT || claim < 0) {
            throw new OrderBusinessException("空投活动缓存未就绪，请稍后重试");
        }

        AirdropClaimMessage message = AirdropClaimMessage.builder()
                .msgId(airdropId + ":" + userId)
                .airdropId(airdropId)
                .userId(userId)
                .couponId(airdropMeta.getCouponId())
                .claimTime(LocalDateTime.now())
                .build();
        boolean sent = sendClaimMessage(message);
        if (!sent) {
            enqueueRetry(message);
        }
    }

    private boolean sendClaimMessage(AirdropClaimMessage message) {
        if (message == null || message.getMsgId() == null || message.getMsgId().isBlank()) {
            return false;
        }
        try {
            CorrelationData correlationData = new CorrelationData(message.getMsgId());
            rabbitTemplate.convertAndSend(
                    RabbitMqConstant.AIRDROP_EXCHANGE,
                    RabbitMqConstant.AIRDROP_CLAIM_ROUTING_KEY,
                    message,
                    m -> {
                        m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return m;
                    },
                    correlationData
            );
            CorrelationData.Confirm confirm = correlationData.getFuture().get(2, TimeUnit.SECONDS);
            return confirm != null && confirm.isAck();
        } catch (Exception e) {
            return false;
        }
    }

    private void enqueueRetry(AirdropClaimMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            stringRedisTemplate.opsForList().leftPush(RedisKeyConstant.airdropClaimMqRetryKey(), json);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void warmupAirdropCache(Long airdropId) {
        if (airdropId == null) {
            return;
        }
        Airdrop airdrop = airdropMapper.getById(airdropId);
        if (airdrop == null) {
            return;
        }
        Coupon coupon = null;
        if (airdrop.getCouponId() != null) {
            coupon = couponMapper.getById(airdrop.getCouponId());
        }
        airdropRedisService.warmupAirdropMeta(airdrop, coupon);
        airdropRedisService.initIfAbsent(airdrop);
    }

    private void scheduleStartIfNeeded(Airdrop airdrop) {
        if (airdrop == null || airdrop.getId() == null) {
            return;
        }
        if (airdrop.getStatus() == null || airdrop.getStatus() != 0) {
            return;
        }
        if (airdrop.getStartTime() == null) {
            return;
        }
        long startMillis = airdrop.getStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        delayQueueService.schedule(DelayTaskTypeConstant.AIRDROP_START, String.valueOf(airdrop.getId()), startMillis, null);
    }

    private void scheduleEndIfNeeded(Airdrop airdrop) {
        if (airdrop == null || airdrop.getId() == null) {
            return;
        }
        if (airdrop.getStatus() != null && (airdrop.getStatus() == 2 || airdrop.getStatus() == 3)) {
            return;
        }
        if (airdrop.getEndTime() == null) {
            return;
        }
        long endMillis = airdrop.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        delayQueueService.schedule(DelayTaskTypeConstant.AIRDROP_END, String.valueOf(airdrop.getId()), endMillis, null);
    }
}
