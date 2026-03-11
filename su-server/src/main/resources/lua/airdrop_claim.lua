local stockKey = KEYS[1]
local userSetKey = KEYS[2]
local userId = ARGV[1]
local ttlSeconds = tonumber(ARGV[2]) or 0
local initStock = tonumber(ARGV[3]) or 0

if userId == false or userId == nil or userId == '' then
  return -4
end

if redis.call('EXISTS', stockKey) == 0 then
  if initStock ~= nil and initStock > 0 then
    redis.call('SET', stockKey, tostring(initStock))
    if ttlSeconds > 0 then
      redis.call('EXPIRE', stockKey, ttlSeconds)
      redis.call('EXPIRE', userSetKey, ttlSeconds)
    end
  else
    return -3
  end
end

local remaining = tonumber(redis.call('GET', stockKey) or '')
if remaining == nil then
  return -3
end

if redis.call('SISMEMBER', userSetKey, userId) == 1 then
  return -1
end

if remaining <= 0 then
  return -2
end

local added = redis.call('SADD', userSetKey, userId)
if added == 0 then
  return -1
end

local newRemaining = tonumber(redis.call('DECR', stockKey))
if ttlSeconds > 0 then
  redis.call('EXPIRE', stockKey, ttlSeconds)
  redis.call('EXPIRE', userSetKey, ttlSeconds)
end

return newRemaining
