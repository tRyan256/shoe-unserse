local queueKey = KEYS[1]
local nowMillis = tonumber(ARGV[1])
local limit = tonumber(ARGV[2])

if limit == nil or limit <= 0 then
  return {}
end

local tasks = redis.call('ZRANGEBYSCORE', queueKey, '-inf', nowMillis, 'LIMIT', 0, limit)
if tasks ~= nil and #tasks > 0 then
  redis.call('ZREM', queueKey, unpack(tasks))
end
return tasks

