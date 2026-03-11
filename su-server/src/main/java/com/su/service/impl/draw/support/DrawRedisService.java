package com.su.service.impl.draw.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.su.constant.RedisKeyConstant;
import com.su.entity.Draw;
import com.su.entity.DrawRecord;
import com.su.utils.cache.CacheClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class DrawRedisService {
    public static final long ALREADY_JOINED = -1L;
    public static final long FULL = -2L;
    public static final long INVALID_ARGUMENT = -3L;

    private static final int UNLIMITED_PLACEHOLDER = 1_000_000_000;
    private static final Duration DEFAULT_MUTATION_TTL = Duration.ofDays(7);
    private static final String STATE_PROCESSED = "processed";
    private static final String STATE_REVEAL_SCHEDULED = "revealScheduled";
    private static final DefaultRedisScript<Long> STATE_MARK_SCRIPT = new DefaultRedisScript<>(
            "local current = redis.call('HGET', KEYS[1], ARGV[1]); if current == '1' then return 0 end; redis.call('HSET', KEYS[1], ARGV[1], '1'); local ttl = tonumber(ARGV[2]); if ttl ~= nil and ttl > 0 then redis.call('EXPIRE', KEYS[1], ttl); end; return 1",
            Long.class
    );

    private final StringRedisTemplate stringRedisTemplate;
    private final CacheClient cacheClient;
    private final ObjectMapper objectMapper;
    private final DefaultRedisScript<Long> reserveAndCacheScript;

    public DrawRedisService(
            StringRedisTemplate stringRedisTemplate,
            CacheClient cacheClient,
            ObjectMapper objectMapper
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.cacheClient = cacheClient;
        this.objectMapper = objectMapper;
        this.reserveAndCacheScript = new DefaultRedisScript<>();
        this.reserveAndCacheScript.setLocation(new ClassPathResource("lua/draw_join_reserve_and_cache.lua"));
        this.reserveAndCacheScript.setResultType(Long.class);
    }

    public boolean hasRuntimeCache(Long drawId) {
        return drawId != null && Boolean.TRUE.equals(stringRedisTemplate.hasKey(remainingKey(drawId)));
    }

    public long reserveAndCacheJoin(Draw draw, DrawRecord record) {
        if (draw == null || draw.getId() == null || record == null || record.getUserId() == null) {
            return INVALID_ARGUMENT;
        }
        Duration ttl = ttlForDraw(draw);
        long ttlSeconds = ttl == null ? 0L : Math.max(1L, ttl.getSeconds());
        String recordJson = toJson(DrawJoinCacheRecord.from(record));
        try {
            Long result = stringRedisTemplate.execute(
                    reserveAndCacheScript,
                    List.of(
                            remainingKey(draw.getId()),
                            participantsKey(draw.getId()),
                            recordsKey(draw.getId()),
                            winnerUsersKey(draw.getId()),
                            stateKey(draw.getId())
                    ),
                    String.valueOf(record.getUserId()),
                    recordJson,
                    String.valueOf(ttlSeconds)
            );
            return result == null ? INVALID_ARGUMENT : result;
        } catch (Exception e) {
            return INVALID_ARGUMENT;
        }
    }

    public void rebuild(Draw draw, List<DrawRecord> records) {
        if (draw == null || draw.getId() == null) {
            return;
        }
        Long drawId = draw.getId();
        deleteForDraw(drawId);

        Duration ttl = ttlForDraw(draw);
        List<DrawRecord> safeRecords = records == null ? List.of() : records;
        LinkedHashMap<Long, DrawRecord> recordByUser = new LinkedHashMap<>();
        for (DrawRecord record : safeRecords) {
            if (record == null || record.getUserId() == null) {
                continue;
            }
            recordByUser.put(record.getUserId(), record);
        }

        int maxParticipants = normalizeMaxParticipants(draw);
        int joinedCount = recordByUser.size();
        int remaining = maxParticipants - joinedCount;
        if (remaining < 0) {
            remaining = 0;
        }
        cacheClient.set(remainingKey(drawId), String.valueOf(remaining), ttl);

        if (!recordByUser.isEmpty()) {
            List<String> participants = new ArrayList<>(recordByUser.size());
            Map<String, String> recordJson = new LinkedHashMap<>();
            List<String> winners = new ArrayList<>();
            for (DrawRecord record : recordByUser.values()) {
                participants.add(String.valueOf(record.getUserId()));
                DrawJoinCacheRecord cacheRecord = DrawJoinCacheRecord.from(record);
                recordJson.put(String.valueOf(record.getUserId()), toJson(cacheRecord));
                if (record.getStatus() != null && record.getStatus() == 1) {
                    winners.add(String.valueOf(record.getUserId()));
                }
            }
            stringRedisTemplate.opsForSet().add(participantsKey(drawId), participants.toArray(String[]::new));
            stringRedisTemplate.opsForHash().putAll(recordsKey(drawId), recordJson);
            if (!winners.isEmpty()) {
                stringRedisTemplate.opsForSet().add(winnerUsersKey(drawId), winners.toArray(String[]::new));
            }
        }

        Map<String, String> state = new LinkedHashMap<>();
        state.put(STATE_PROCESSED, isProcessed(draw) ? "1" : "0");
        state.put(STATE_REVEAL_SCHEDULED, shouldMarkRevealScheduled(draw, remaining) ? "1" : "0");
        stringRedisTemplate.opsForHash().putAll(stateKey(drawId), state);
        expireKeys(ttl,
                remainingKey(drawId),
                participantsKey(drawId),
                recordsKey(drawId),
                winnerUsersKey(drawId),
                stateKey(drawId));
    }

    public List<DrawRecord> loadCachedRecordsForSync(Draw draw) {
        if (draw == null || draw.getId() == null) {
            return List.of();
        }
        LinkedHashMap<Long, DrawRecord> merged = new LinkedHashMap<>();
        for (DrawRecord record : loadNewCachedRecords(draw.getId())) {
            if (record != null && record.getId() != null) {
                merged.put(record.getId(), record);
            }
        }
        for (DrawRecord record : loadLegacyCachedRecords(draw.getId())) {
            if (record != null && record.getId() != null) {
                merged.putIfAbsent(record.getId(), record);
            }
        }
        return new ArrayList<>(merged.values());
    }


    public DrawRecord getRecord(Long drawId, Long userId) {
        if (drawId == null || userId == null) {
            return null;
        }
        Object json = stringRedisTemplate.opsForHash().get(recordsKey(drawId), String.valueOf(userId));
        if (json == null) {
            return null;
        }
        DrawJoinCacheRecord cacheRecord = fromJson(String.valueOf(json));
        return cacheRecord == null ? null : cacheRecord.toDrawRecord();
    }


    public void updateRecordOnReveal(Long drawId, Long userId, Integer status) {
        updateRecord(drawId, userId, record -> {
            record.setStatus(status);
            if (status != null && status == 1) {
                stringRedisTemplate.opsForSet().add(winnerUsersKey(drawId), String.valueOf(userId));
            } else {
                stringRedisTemplate.opsForSet().remove(winnerUsersKey(drawId), String.valueOf(userId));
            }
        });
    }

    public void updateRecordOnWinConfirm(Long drawId, Long userId, String shoeSize, String orderNo, Long addressBookId) {
        updateRecord(drawId, userId, record -> {
            record.setStatus(1);
            record.setOrderNo(orderNo);
            if (shoeSize != null && !shoeSize.isBlank()) {
                record.setShoeSize(shoeSize);
            }
            if (addressBookId != null) {
                record.setAddressBookId(addressBookId);
            }
            stringRedisTemplate.opsForSet().add(winnerUsersKey(drawId), String.valueOf(userId));
        });
    }

    public void updateRecordOnTimeoutRollback(Long drawId, Long userId) {
        updateRecord(drawId, userId, record -> {
            record.setStatus(1);
            record.setOrderNo(null);
            stringRedisTemplate.opsForSet().add(winnerUsersKey(drawId), String.valueOf(userId));
        });
    }

    public DrawResult getResult(Long drawId, Long userId) {
        DrawRecord record = getRecord(drawId, userId);
        if (record == null || record.getStatus() == null) {
            return null;
        }
        return new DrawResult(record.getStatus(), record.getOrderNo());
    }

    public void deleteForDraw(Long drawId) {
        if (drawId == null) {
            return;
        }
        List<DrawRecord> cachedRecords = loadCachedRecordsForDelete(drawId);
        for (DrawRecord record : cachedRecords) {
            if (record == null || record.getUserId() == null) {
                continue;
            }
            cacheClient.evict(legacyDrawUserRecordsKey(record.getUserId()));
        }
        stringRedisTemplate.delete(newKeySet(drawId));
        stringRedisTemplate.delete(legacyKeySet(drawId));
        for (DrawRecord record : cachedRecords) {
            if (record == null) {
                continue;
            }
            if (record.getId() != null) {
                cacheClient.evict(legacyDrawRecordKey(record.getId()));
            }
            if (record.getUserId() != null) {
                cacheClient.evict(legacyDrawJoinRecordKey(drawId, record.getUserId()));
            }
        }
    }

    public void deleteLegacyForDraw(Long drawId) {
        if (drawId == null) {
            return;
        }
        List<DrawRecord> legacyRecords = loadLegacyCachedRecords(drawId);
        for (DrawRecord record : legacyRecords) {
            if (record == null) {
                continue;
            }
            if (record.getId() != null) {
                cacheClient.evict(legacyDrawRecordKey(record.getId()));
                if (record.getUserId() != null) {
                    stringRedisTemplate.opsForZSet().remove(legacyDrawUserRecordsKey(record.getUserId()), String.valueOf(record.getId()));
                }
            }
            if (record.getUserId() != null) {
                cacheClient.evict(legacyDrawJoinRecordKey(drawId, record.getUserId()));
            }
        }
        stringRedisTemplate.delete(legacyKeySet(drawId));
    }

    public String remainingKey(Long drawId) {
        return RedisKeyConstant.drawQuotaRemainingKey(drawId);
    }

    public String participantsKey(Long drawId) {
        return RedisKeyConstant.drawParticipantsKey(drawId);
    }

    public String recordsKey(Long drawId) {
        return RedisKeyConstant.drawRecordsKey(drawId);
    }


    public String winnerUsersKey(Long drawId) {
        return RedisKeyConstant.drawWinnersKey(drawId);
    }

    public String stateKey(Long drawId) {
        return RedisKeyConstant.drawStateKey(drawId);
    }

    public String processedKey(Long drawId) {
        return RedisKeyConstant.drawStateKey(drawId);
    }

    public String revealScheduledKey(Long drawId) {
        return RedisKeyConstant.drawStateKey(drawId);
    }

    public boolean markProcessed(Long drawId) {
        return markStateFlag(drawId, STATE_PROCESSED, DEFAULT_MUTATION_TTL);
    }

    public boolean markRevealScheduled(Long drawId) {
        return markStateFlag(drawId, STATE_REVEAL_SCHEDULED, Duration.ofDays(1));
    }

    public String legacyDrawRemainingKey(Long drawId) {
        return RedisKeyConstant.legacyDrawRemainingKey(drawId);
    }

    public String legacyDrawParticipantsKey(Long drawId) {
        return RedisKeyConstant.legacyDrawParticipantsKey(drawId);
    }

    public String legacyDrawJoinUsersKey(Long drawId) {
        return RedisKeyConstant.legacyDrawJoinUsersKey(drawId);
    }

    public String legacyDrawJoinRecordKey(Long drawId, Long userId) {
        return drawId == null || userId == null ? null : RedisKeyConstant.legacyDrawJoinRecordKey(drawId, userId);
    }

    public String legacyDrawResultKey(Long drawId) {
        return RedisKeyConstant.legacyDrawResultKey(drawId);
    }

    public String legacyDrawRecordKey(Long recordId) {
        return RedisKeyConstant.legacyDrawRecordKey(recordId);
    }

    public String legacyDrawRecordKeyPrefix() {
        return RedisKeyConstant.legacyDrawRecordKeyPrefix();
    }

    public String legacyDrawWinnerUsersKey(Long drawId) {
        return RedisKeyConstant.legacyDrawWinnerUsersKey(drawId);
    }

    public String legacyDrawProcessedKey(Long drawId) {
        return RedisKeyConstant.legacyDrawProcessedKey(drawId);
    }

    public String legacyDrawRevealScheduledKey(Long drawId) {
        return RedisKeyConstant.legacyDrawRevealScheduledKey(drawId);
    }

    public String legacyDrawUserRecordsKey(Long userId) {
        return userId == null ? null : RedisKeyConstant.legacyDrawUserRecordsKey(userId);
    }

    public record DrawResult(Integer status, String orderNo) {
    }

    private void writeRecord(Long drawId, DrawRecord record, Duration ttl) {
        if (drawId == null || record == null || record.getUserId() == null) {
            return;
        }
        DrawJoinCacheRecord cacheRecord = DrawJoinCacheRecord.from(record);
        stringRedisTemplate.opsForHash().put(recordsKey(drawId), String.valueOf(record.getUserId()), toJson(cacheRecord));
        expireKeys(ttl, recordsKey(drawId));
    }

    private void updateRecord(Long drawId, Long userId, java.util.function.Consumer<DrawRecord> updater) {
        if (drawId == null || userId == null || updater == null) {
            return;
        }
        DrawRecord record = getRecord(drawId, userId);
        if (record == null) {
            return;
        }
        updater.accept(record);
        writeRecord(drawId, record, resolveMutationTtl(drawId));
        expireKeys(resolveMutationTtl(drawId), winnerUsersKey(drawId), stateKey(drawId));
    }

    private List<DrawRecord> loadCachedRecordsForDelete(Long drawId) {
        LinkedHashMap<Long, DrawRecord> merged = new LinkedHashMap<>();
        for (DrawRecord record : loadNewCachedRecords(drawId)) {
            if (record != null && record.getId() != null) {
                merged.put(record.getId(), record);
            }
        }
        for (DrawRecord record : loadLegacyCachedRecords(drawId)) {
            if (record != null && record.getId() != null) {
                merged.putIfAbsent(record.getId(), record);
            }
        }
        return new ArrayList<>(merged.values());
    }

    private List<DrawRecord> loadNewCachedRecords(Long drawId) {
        if (drawId == null) {
            return List.of();
        }
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(recordsKey(drawId));
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        ArrayList<DrawRecord> records = new ArrayList<>(entries.size());
        for (Object value : entries.values()) {
            if (value == null) {
                continue;
            }
            DrawJoinCacheRecord cacheRecord = fromJson(String.valueOf(value));
            if (cacheRecord == null) {
                continue;
            }
            DrawRecord record = cacheRecord.toDrawRecord();
            if (record.getDrawId() == null) {
                record.setDrawId(drawId);
            }
            if (record.getStatus() == null) {
                record.setStatus(0);
            }
            if (record.getCreateTime() == null) {
                record.setCreateTime(LocalDateTime.now());
            }
            records.add(record);
        }
        return records;
    }

    private List<DrawRecord> loadLegacyCachedRecords(Long drawId) {
        if (drawId == null) {
            return List.of();
        }
        Set<String> legacyRecordIds = stringRedisTemplate.opsForSet().members(legacyDrawParticipantsKey(drawId));
        if (legacyRecordIds == null || legacyRecordIds.isEmpty()) {
            return List.of();
        }
        ArrayList<DrawRecord> records = new ArrayList<>(legacyRecordIds.size());
        for (String recordIdStr : legacyRecordIds) {
            Long recordId = parseLong(recordIdStr);
            if (recordId == null) {
                continue;
            }
            Map<Object, Object> entry = stringRedisTemplate.opsForHash().entries(legacyDrawRecordKey(recordId));
            if (entry == null || entry.isEmpty()) {
                continue;
            }
            Long userId = parseLong(entry.get("userId"));
            if (userId == null) {
                continue;
            }
            DrawRecord record = DrawRecord.builder()
                    .id(recordId)
                    .drawId(drawId)
                    .userId(userId)
                    .addressBookId(parseLong(entry.get("addressBookId")))
                    .shoeSize(parseString(entry.get("shoeSize")))
                    .skuId(parseLong(entry.get("skuId")))
                    .status(parseInt(entry.get("status"), 0))
                    .orderNo(parseString(entry.get("orderNo")))
                    .createTime(parseTime(entry.get("createTime")))
                    .build();
            if (record.getCreateTime() == null) {
                record.setCreateTime(LocalDateTime.now());
            }
            records.add(record);
        }
        return records;
    }

    private boolean markStateFlag(Long drawId, String field, Duration fallbackTtl) {
        if (drawId == null || field == null || field.isBlank()) {
            return false;
        }
        Duration ttl = resolveStateTtl(drawId, fallbackTtl);
        Long result = stringRedisTemplate.execute(
                STATE_MARK_SCRIPT,
                List.of(stateKey(drawId)),
                field,
                String.valueOf(Math.max(0L, ttl.toSeconds()))
        );
        return result != null && result == 1L;
    }

    private Duration resolveMutationTtl(Long drawId) {
        Duration ttl = resolveExistingTtl(recordsKey(drawId), DEFAULT_MUTATION_TTL);
        if (ttl.compareTo(Duration.ofHours(1)) < 0) {
            ttl = Duration.ofHours(1);
        }
        return ttl;
    }

    private Duration resolveStateTtl(Long drawId, Duration fallback) {
        Duration ttl = resolveExistingTtl(stateKey(drawId), fallback == null ? DEFAULT_MUTATION_TTL : fallback);
        if (ttl.compareTo(Duration.ofHours(1)) < 0) {
            ttl = Duration.ofHours(1);
        }
        return ttl;
    }

    private Duration resolveExistingTtl(String key, Duration fallback) {
        if (key == null || key.isBlank()) {
            return fallback;
        }
        Long seconds = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (seconds == null || seconds <= 0) {
            return fallback;
        }
        return Duration.ofSeconds(seconds);
    }

    private boolean isProcessed(Draw draw) {
        return draw != null && draw.getStatus() != null && draw.getStatus() == 2;
    }

    private boolean shouldMarkRevealScheduled(Draw draw, int remaining) {
        if (draw == null) {
            return false;
        }
        if (isProcessed(draw)) {
            return true;
        }
        return draw.getStatus() != null && draw.getStatus() == 1 && remaining <= 0;
    }

    private void expireKeys(Duration ttl, String... keys) {
        if (ttl == null || keys == null) {
            return;
        }
        for (String key : keys) {
            if (key != null && !key.isBlank()) {
                cacheClient.expire(key, ttl);
            }
        }
    }

    private Collection<String> newKeySet(Long drawId) {
        if (drawId == null) {
            return Collections.emptyList();
        }
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        keys.add(remainingKey(drawId));
        keys.add(participantsKey(drawId));
        keys.add(recordsKey(drawId));
        keys.add(winnerUsersKey(drawId));
        keys.add(stateKey(drawId));
        return keys;
    }

    private Collection<String> legacyKeySet(Long drawId) {
        if (drawId == null) {
            return Collections.emptyList();
        }
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        keys.add(legacyDrawRemainingKey(drawId));
        keys.add(legacyDrawParticipantsKey(drawId));
        keys.add(legacyDrawJoinUsersKey(drawId));
        keys.add(legacyDrawResultKey(drawId));
        keys.add(legacyDrawWinnerUsersKey(drawId));
        keys.add(legacyDrawProcessedKey(drawId));
        keys.add(legacyDrawRevealScheduledKey(drawId));
        return keys;
    }

    private Integer normalizeMaxParticipants(Draw draw) {
        if (draw == null) {
            return UNLIMITED_PLACEHOLDER;
        }
        Integer maxParticipants = draw.getMaxParticipants();
        if (maxParticipants != null && maxParticipants > 0) {
            return maxParticipants;
        }
        return UNLIMITED_PLACEHOLDER;
    }

    private Duration ttlForDraw(Draw draw) {
        Duration ttl = Duration.ofDays(2);
        if (draw != null && draw.getEndTime() != null) {
            long endMillis = draw.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long nowMillis = System.currentTimeMillis();
            if (endMillis > nowMillis) {
                ttl = Duration.ofMillis(endMillis - nowMillis).plusDays(2);
            }
        }
        if (ttl.compareTo(Duration.ofHours(1)) < 0) {
            ttl = Duration.ofHours(1);
        }
        return ttl;
    }

    private long toEpochMilli(LocalDateTime time) {
        LocalDateTime safeTime = time == null ? LocalDateTime.now() : time;
        return safeTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private String toJson(DrawJoinCacheRecord record) {
        try {
            return objectMapper.writeValueAsString(record);
        } catch (Exception e) {
            throw new IllegalStateException("serialize draw cache record failed", e);
        }
    }

    private DrawJoinCacheRecord fromJson(String json) {
        try {
            return json == null || json.isBlank() ? null : objectMapper.readValue(json, DrawJoinCacheRecord.class);
        } catch (Exception e) {
            return null;
        }
    }


    private Long parseLong(Object value) {
        try {
            return value == null ? null : Long.valueOf(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInt(Object value, int fallback) {
        try {
            return value == null ? fallback : Integer.valueOf(String.valueOf(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    private LocalDateTime parseTime(Object value) {
        try {
            String text = parseString(value);
            return text == null ? null : LocalDateTime.parse(text);
        } catch (Exception e) {
            return null;
        }
    }

    private String parseString(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        return text.isBlank() ? null : text;
    }
}






