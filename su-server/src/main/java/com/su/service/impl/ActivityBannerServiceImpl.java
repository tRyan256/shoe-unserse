package com.su.service.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.su.constant.RedisKeyConstant;
import com.su.entity.Airdrop;
import com.su.entity.Coupon;
import com.su.entity.Draw;
import com.su.mapper.AirdropMapper;
import com.su.mapper.CouponMapper;
import com.su.mapper.DrawMapper;
import com.su.service.ActivityBannerService;
import com.su.service.airdrop.support.AirdropMeta;
import com.su.service.airdrop.support.CouponMeta;
import com.su.utils.cache.CacheClient;
import com.su.utils.cache.LogicalExpire;
import com.su.vo.ActivityBannerVO;
import com.su.vo.DrawBundlePublicVO;
import com.su.vo.DrawDetailVO;
import com.su.vo.DrawShoePublicVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class ActivityBannerServiceImpl implements ActivityBannerService {

    private final DrawMapper drawMapper;
    private final AirdropMapper airdropMapper;
    private final CouponMapper couponMapper;
    private final CacheClient cacheClient;
    private final ObjectMapper objectMapper;

    public ActivityBannerServiceImpl(
            DrawMapper drawMapper,
            AirdropMapper airdropMapper,
            CouponMapper couponMapper,
            CacheClient cacheClient,
            ObjectMapper objectMapper
    ) {
        this.drawMapper = drawMapper;
        this.airdropMapper = airdropMapper;
        this.couponMapper = couponMapper;
        this.cacheClient = cacheClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ActivityBannerVO> getBannerActivities() {
        List<ActivityBannerVO> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        try {
            processDrawActivities(result, now);
            processAirdropActivities(result, now);
            result.sort(Comparator.comparing(ActivityBannerVO::getStartTime));
        } catch (Exception e) {
            log.error("Error in getBannerActivities: {}", e.getMessage(), e);
        }

        if (result.isEmpty()) {
            result.add(getDefaultBanner());
        }

        return result;
    }

    private ActivityBannerVO getDefaultBanner() {
        return ActivityBannerVO.builder()
                .id(0L)
                .type("default")
                .title("鞋宙")
                .description("探索限量球鞋世界，发现专属你的潮流单品")
                .image("/images/default-banner.svg")
                .bannerStatus("default")
                .build();
    }

    private void processDrawActivities(List<ActivityBannerVO> result, LocalDateTime now) {
        List<Draw> drawList = drawMapper.listWarmup(now.minusDays(1), now.plusDays(2));
        if (drawList == null || drawList.isEmpty()) {
            return;
        }

        for (Draw draw : drawList) {
            DrawDetailVO detail = getDrawDetailFromCache(draw.getId());
            if (detail == null) {
                continue;
            }

            Integer status = detail.getStatus();
            LocalDateTime startTime = detail.getStartTime();
            LocalDateTime drawTime = detail.getDrawTime();
            LocalDateTime endTime = detail.getEndTime();

            if (status != null && status == 0 && isUpcomingWithinHours(startTime, now, 48)) {
                result.add(convertDrawToBanner(detail, "warmup"));
            } else if (status != null && status == 2 && isRecentPastWithinHours(drawTime != null ? drawTime : endTime, now, 24)) {
                result.add(convertDrawToBanner(detail, "drawn"));
            }
        }
    }

    private DrawDetailVO getDrawDetailFromCache(Long drawId) {
        if (drawId == null) {
            return null;
        }
        String key = RedisKeyConstant.drawDetailKey(drawId);
        try {
            String json = cacheClient.get(key);
            if (json == null || json.isBlank()) {
                return null;
            }
            JavaType wrapperType = objectMapper.getTypeFactory().constructParametricType(
                    LogicalExpire.class, DrawDetailVO.class);
            LogicalExpire<DrawDetailVO> logical = objectMapper.readValue(json, wrapperType);
            return logical == null ? null : logical.getData();
        } catch (Exception e) {
            log.error("Failed to parse draw detail from cache: {}", e.getMessage(), e);
            return null;
        }
    }

    private void processAirdropActivities(List<ActivityBannerVO> result, LocalDateTime now) {
        List<Airdrop> airdropList = airdropMapper.listForBanner();
        if (airdropList == null || airdropList.isEmpty()) {
            return;
        }

        for (Airdrop airdrop : airdropList) {
            AirdropMeta meta = getAirdropMeta(airdrop);
            if (meta == null) {
                continue;
            }

            Integer status = meta.getStatus();
            LocalDateTime startTime = meta.getStartTime();
            LocalDateTime endTime = meta.getEndTime();
            Integer remainCount = meta.getRemainCount();

            if (status != null && status == 1 && isNowWithin(startTime, endTime, now) && (remainCount == null || remainCount > 0)) {
                result.add(convertAirdropToBanner(meta, "active"));
            } else if (status != null && status == 0 && isUpcomingWithinHours(startTime, now, 48)) {
                result.add(convertAirdropToBanner(meta, "warmup"));
            } else if (isRecentPastWithinHours(endTime, now, 24)
                    && (status != null && status == 2
                    || (endTime != null && now.isAfter(endTime))
                    || (remainCount != null && remainCount <= 0))) {
                result.add(convertAirdropToBanner(meta, "drawn"));
            }
        }
    }

    private AirdropMeta getAirdropMeta(Airdrop airdrop) {
        if (airdrop == null || airdrop.getId() == null) {
            return null;
        }
        AirdropMeta cached = getAirdropMetaFromCache(airdrop.getId());
        if (cached != null) {
            return cached;
        }

        Coupon coupon = null;
        if (airdrop.getCouponId() != null) {
            coupon = couponMapper.getById(airdrop.getCouponId());
        }
        return buildAirdropMetaFromDb(airdrop, coupon);
    }

    private AirdropMeta getAirdropMetaFromCache(Long airdropId) {
        if (airdropId == null) {
            return null;
        }
        String key = RedisKeyConstant.airdropMetaKey(airdropId);
        try {
            String json = cacheClient.get(key);
            if (json == null || json.isBlank()) {
                return null;
            }
            JavaType wrapperType = objectMapper.getTypeFactory().constructParametricType(
                    LogicalExpire.class, AirdropMeta.class);
            LogicalExpire<AirdropMeta> logical = objectMapper.readValue(json, wrapperType);
            return logical == null ? null : logical.getData();
        } catch (Exception e) {
            log.error("Failed to parse airdrop meta from cache: {}", e.getMessage(), e);
            return null;
        }
    }

    private AirdropMeta buildAirdropMetaFromDb(Airdrop airdrop, Coupon coupon) {
        if (airdrop == null || airdrop.getId() == null) {
            return null;
        }

        CouponMeta couponMeta = null;
        if (coupon != null) {
            couponMeta = CouponMeta.builder()
                    .id(coupon.getId())
                    .name(coupon.getName())
                    .type(coupon.getType())
                    .value(coupon.getValue())
                    .minAmount(coupon.getMinAmount())
                    .startTime(coupon.getStartTime())
                    .endTime(coupon.getEndTime())
                    .status(coupon.getStatus())
                    .createTime(coupon.getCreateTime())
                    .updateTime(coupon.getUpdateTime())
                    .build();
        }

        return AirdropMeta.builder()
                .id(airdrop.getId())
                .title(airdrop.getTitle())
                .couponId(airdrop.getCouponId())
                .status(airdrop.getStatus())
                .remainCount(airdrop.getRemainCount())
                .startTime(airdrop.getStartTime())
                .endTime(airdrop.getEndTime())
                .coupon(couponMeta)
                .build();
    }

    private boolean isUpcomingWithinHours(LocalDateTime targetTime, LocalDateTime now, int hours) {
        if (targetTime == null) {
            return false;
        }
        return !targetTime.isBefore(now) && !targetTime.isAfter(now.plusHours(hours));
    }

    private boolean isRecentPastWithinHours(LocalDateTime targetTime, LocalDateTime now, int hours) {
        if (targetTime == null) {
            return false;
        }
        return !targetTime.isAfter(now) && !targetTime.isBefore(now.minusHours(hours));
    }

    private boolean isNowWithin(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime now) {
        boolean started = startTime == null || !now.isBefore(startTime);
        boolean notEnded = endTime == null || !now.isAfter(endTime);
        return started && notEnded;
    }

    private ActivityBannerVO convertDrawToBanner(DrawDetailVO detail, String status) {
        String image = null;
        if (detail.getTargetType() != null) {
            if (detail.getTargetType() == 1) {
                DrawShoePublicVO shoe = detail.getShoe();
                if (shoe != null) {
                    image = shoe.getImage();
                }
            } else if (detail.getTargetType() == 2) {
                DrawBundlePublicVO bundle = detail.getBundle();
                if (bundle != null) {
                    image = bundle.getImage();
                }
            }
        }

        return ActivityBannerVO.builder()
                .id(detail.getId())
                .type("draw")
                .title(detail.getTitle())
                .description(detail.getDescription())
                .image(image)
                .startTime(detail.getStartTime())
                .endTime(detail.getEndTime())
                .drawTime(detail.getDrawTime())
                .bannerStatus(status)
                .build();
    }

    private ActivityBannerVO convertAirdropToBanner(AirdropMeta meta, String status) {
        String title = meta.getTitle();
        if (title == null || title.isBlank()) {
            CouponMeta coupon = meta.getCoupon();
            if (coupon != null && coupon.getName() != null) {
                title = coupon.getName();
            }
        }
        if (title == null || title.isBlank()) {
            title = "空投福利";
        }

        return ActivityBannerVO.builder()
                .id(meta.getId())
                .type("airdrop")
                .title(title)
                .description("限时空投福利")
                .image("/images/airdrop-default-bg.svg")
                .startTime(meta.getStartTime())
                .endTime(meta.getEndTime())
                .bannerStatus(status)
                .build();
    }
}
