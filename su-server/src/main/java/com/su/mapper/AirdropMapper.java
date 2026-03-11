package com.su.mapper;

import com.github.pagehelper.Page;
import com.su.dto.AirdropPageQueryDTO;
import com.su.entity.Airdrop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AirdropMapper {
    void insert(Airdrop airdrop);

    void update(Airdrop airdrop);

    void deleteById(Long id);

    @Select("select * from airdrop where id = #{id}")
    Airdrop getById(Long id);

    Page<Airdrop> pageQuery(AirdropPageQueryDTO dto);

    @Update("update airdrop set remain_count = #{remainCount}, update_time = now() where id = #{id}")
    void updateRemainCount(Long id, Integer remainCount);

    @Select("select * from airdrop where status = 1 and remain_count > 0 and (start_time is null or start_time <= now()) and (end_time is null or end_time >= now())")
    List<Airdrop> listRunning();

    @Select("select count(1) from airdrop " +
            "where coupon_id = #{couponId} " +
            "and status <> 3 " +
            "and (start_time is null or start_time <= date_add(now(), interval 2 day)) " +
            "and ((end_time is not null and end_time >= date_sub(now(), interval 1 day)) " +
            "or (end_time is null and status in (0, 1)))")
    int countWarmupByCouponId(Long couponId);

    /**
     * 查询需要预热的空投活动
     * 预热条件：活动开始前2天到活动结束后1天
     */
    @Select("select * from airdrop where status in (0, 1) " +
            "and (start_time is null or start_time <= date_add(now(), interval 2 day)) " +
            "and (end_time is null or end_time >= date_sub(now(), interval 1 day))")
    List<Airdrop> listWarmup();

    /**
     * 查询轮播图需要的空投活动
     * 包括：未开始(48小时内)、进行中、已结束(24小时内)
     */
    @Select("select * from airdrop where status in (0, 1, 2) " +
            "and (start_time is null or start_time <= date_add(now(), interval 2 day)) " +
            "and (end_time is null or end_time >= date_sub(now(), interval 1 day))")
    List<Airdrop> listForBanner();
}
