package com.su.mq.service;

import com.su.entity.AirdropRecord;
import com.su.entity.UserCoupon;
import com.su.mapper.AirdropRecordMapper;
import com.su.mapper.UserCouponMapper;
import com.su.mq.message.AirdropClaimMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AirdropClaimPersistService {
    private static final int BATCH_SIZE = 200;

    private final AirdropRecordMapper airdropRecordMapper;
    private final UserCouponMapper userCouponMapper;

    public AirdropClaimPersistService(AirdropRecordMapper airdropRecordMapper, UserCouponMapper userCouponMapper) {
        this.airdropRecordMapper = airdropRecordMapper;
        this.userCouponMapper = userCouponMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public void persist(AirdropClaimMessage message) {
        persistBatch(message == null ? List.of() : List.of(message));
    }

    @Transactional(rollbackFor = Exception.class)
    public void persistBatch(List<AirdropClaimMessage> messages) {
        List<AirdropClaimMessage> deduped = normalize(messages);
        if (deduped.isEmpty()) {
            return;
        }
        for (int i = 0; i < deduped.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, deduped.size());
            List<AirdropClaimMessage> batch = deduped.subList(i, end);
            for (AirdropClaimMessage message : batch) {
                persistOne(message);
            }
        }
    }

    private List<AirdropClaimMessage> normalize(List<AirdropClaimMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }
        Map<String, AirdropClaimMessage> deduped = new LinkedHashMap<>();
        for (AirdropClaimMessage message : messages) {
            if (message == null || message.getAirdropId() == null || message.getUserId() == null || message.getCouponId() == null) {
                continue;
            }
            String dedupeKey = message.getAirdropId() + ":" + message.getUserId();
            deduped.putIfAbsent(dedupeKey, message);
        }
        return new ArrayList<>(deduped.values());
    }

    private void persistOne(AirdropClaimMessage message) {
        AirdropRecord record = airdropRecordMapper.getByAirdropIdAndUserId(message.getAirdropId(), message.getUserId());
        if (record == null) {
            try {
                AirdropRecord toInsert = AirdropRecord.builder()
                        .airdropId(message.getAirdropId())
                        .userId(message.getUserId())
                        .userCouponId(null)
                        .createTime(message.getClaimTime() == null ? LocalDateTime.now() : message.getClaimTime())
                        .build();
                airdropRecordMapper.insert(toInsert);
                record = toInsert;
            } catch (DuplicateKeyException e) {
                log.info("空投领取记录已存在，airdropId={}, userId={}", message.getAirdropId(), message.getUserId());
                record = airdropRecordMapper.getByAirdropIdAndUserId(message.getAirdropId(), message.getUserId());
            }
        }

        if (record != null && record.getUserCouponId() != null) {
            return;
        }

        UserCoupon userCoupon = userCouponMapper.getUnusedByUserIdAndCouponId(message.getUserId(), message.getCouponId());
        if (userCoupon == null) {
            userCoupon = UserCoupon.builder()
                    .userId(message.getUserId())
                    .couponId(message.getCouponId())
                    .status(0)
                    .createTime(LocalDateTime.now())
                    .build();
            userCouponMapper.insert(userCoupon);
        }

        airdropRecordMapper.updateUserCouponId(message.getAirdropId(), message.getUserId(), userCoupon.getId());
    }
}
