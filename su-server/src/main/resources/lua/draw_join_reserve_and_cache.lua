local remainingKey = KEYS[1]
local userSetKey = KEYS[2]
local recordHashKey = KEYS[3]
local winnerSetKey = KEYS[4]
local stateKey = KEYS[5]

local userId = ARGV[1]
local recordJson = ARGV[2]
local ttlSeconds = tonumber(ARGV[3]) or 0

if userId == false or userId == nil or userId == '' then
  return -3
end
if recordJson == false or recordJson == nil or recordJson == '' then
  return -3
end

if redis.call('SISMEMBER', userSetKey, userId) == 1 then
  return -1
end

local remainingRaw = redis.call('GET', remainingKey)
if remainingRaw == false or remainingRaw == nil then
  return -3
end

local remaining = tonumber(remainingRaw)
if remaining == nil then
  return -3
end

if remaining <= 0 then
  return -2
end

local added = redis.call('SADD', userSetKey, userId)
if added == 0 then
  return -1
end

local newRemaining = tonumber(redis.call('DECR', remainingKey))
redis.call('HSET', recordHashKey, userId, recordJson)

if ttlSeconds > 0 then
  redis.call('EXPIRE', remainingKey, ttlSeconds)
  redis.call('EXPIRE', userSetKey, ttlSeconds)
  redis.call('EXPIRE', recordHashKey, ttlSeconds)
  redis.call('EXPIRE', winnerSetKey, ttlSeconds)
  redis.call('EXPIRE', stateKey, ttlSeconds)
end

return newRemaining
