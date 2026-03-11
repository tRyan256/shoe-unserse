package com.su.task.airdrop;

import com.su.constant.RedisKeyConstant;
import com.su.entity.Airdrop;
import com.su.mapper.AirdropMapper;
import com.su.utils.cache.CacheClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AirdropStockSyncTask {
    private final AirdropMapper airdropMapper;
    private final CacheClient cacheClient;

    public AirdropStockSyncTask(AirdropMapper airdropMapper, CacheClient cacheClient) {
        this.airdropMapper = airdropMapper;
        this.cacheClient = cacheClient;
    }

    @Scheduled(cron = "0 */1 * * * ?")
    public void syncRemainCount() {
        List<Airdrop> list = airdropMapper.listRunning();
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Airdrop airdrop : list) {
            if (airdrop == null || airdrop.getId() == null) {
                continue;
            }
            String stock = cacheClient.get(RedisKeyConstant.airdropStockKey(airdrop.getId()));
            if (stock == null || stock.isBlank()) {
                continue;
            }
            try {
                int remain = Integer.parseInt(stock);
                airdropMapper.updateRemainCount(airdrop.getId(), remain);
            } catch (Exception ignored) {
            }
        }
    }
}

