# Multi-Level Cache for Draw/Airdrop Details Design

**Date:** 2026-03-16
**Project:** ShoeUniverse (su-server)

## Goal
Add L1 (Caffeine) caches for Draw detail and Airdrop meta reads, with Redis logical-expire as L2, to reduce Redis/DB load under hot reads while keeping acceptable short staleness.

## Non-Goals
- No MQ-based cache invalidation fan-out
- No global cache abstraction refactor (keep changes localized)
- No changes to Redis logical-expire strategy

## Current State
- `DrawServiceImpl.detail` reads L2 using `CacheClient.queryWithLogicalExpire`.
- `AirdropRedisService.getMeta` reads L2 using `CacheClient.getLogicalExpireValue`.
- `LocalCacheConfiguration` defines only `categoryLocalCache`.

## Proposed Design
### L1 Caches (Caffeine)
- `drawDetailLocalCache`: `maximumSize=2000`, `expireAfterWrite=10s`, **non-null only**.
- `airdropMetaLocalCache`: `maximumSize=5000`, `expireAfterWrite=10s`, **non-null only**.

### Read Path
- **L1 hit**: return immediately.
- **L1 miss**: read L2 via existing logical-expire logic. If non-null, backfill L1.

### Warmup Path
- Write to L2 (logical-expire) first.
- If non-null, then put into L1.

### Evict Path
- Delete L2 first.
- Then invalidate L1 (explicitly after L2 delete to avoid race-based dirty reads).

## Components & Files
- `su-server/src/main/java/com/su/config/LocalCacheConfiguration.java`
  - Add Caffeine beans for `drawDetailLocalCache` and `airdropMetaLocalCache`.
- `su-server/src/main/java/com/su/service/impl/DrawServiceImpl.java`
  - Inject `Cache<Long, DrawDetailVO>`.
  - L1 read-through in `detail`.
  - L1 write in `warmupDrawCache`.
  - L1 invalidate **after** L2 delete in `clearDrawCache`.
- `su-server/src/main/java/com/su/service/airdrop/support/AirdropRedisService.java`
  - Inject `Cache<Long, AirdropMeta>`.
  - L1 read-through in `getMeta`.
  - L1 write in `warmupAirdropMeta`.
  - L1 invalidate after `delete`.

## Consistency Model
- L1 can be stale up to ~10s across nodes.
- L2 remains the source of truth; cache-aside deletion used on writes.
- L1 only caches non-null to avoid hiding newly created data.

## Error Handling
- If L1 operations fail, fall back to L2 behavior.
- Existing logical-expire async rebuild remains unchanged.

## Testing Plan
- Unit tests for L1 hit/miss behavior in `DrawServiceImpl` and `AirdropRedisService`.
- Unit tests for L1 invalidate/put on warmup/delete paths.
- Existing test suite run: `mvn -pl su-server test -Dmaven.repo.local=D:\code\shoe_universe\.m2repo`.

## Rollout
- Code-only change; no data migration required.
- Monitor Redis QPS and cache hit ratio after deployment.
