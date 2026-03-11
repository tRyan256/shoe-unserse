package com.su.mapper;

import com.su.entity.AirdropRecord;
import com.su.vo.AirdropRecordVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AirdropRecordMapper {
    void insert(AirdropRecord record);

    boolean exists(Long airdropId, Long userId);

    void updateUserCouponId(Long airdropId, Long userId, Long userCouponId);

    int countByAirdropId(Long airdropId);

    AirdropRecord getByAirdropIdAndUserId(Long airdropId, Long userId);

    /**
     * List airdrop records with joined airdrop and coupon details for user profile display
     * @param userId User ID
     * @return List of AirdropRecordVO with complete airdrop and coupon information, sorted by create_time desc
     */
    List<AirdropRecordVO> listByUserIdWithDetails(Long userId);
}
