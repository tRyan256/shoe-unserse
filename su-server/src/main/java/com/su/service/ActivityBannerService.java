package com.su.service;

import com.su.vo.ActivityBannerVO;

import java.util.List;

public interface ActivityBannerService {
    /**
     * 获取轮播图活动列表
     * 从现有缓存中获取活动数据，根据业务规则过滤
     */
    List<ActivityBannerVO> getBannerActivities();
}
