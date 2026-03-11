if redis.replicate_commands ~= nil then
  redis.replicate_commands()
end

local participantsKey = KEYS[1]
local winnerUsersKey = KEYS[2]
local winnerCount = tonumber(ARGV[1])

if winnerCount == nil or winnerCount <= 0 then
  return {}
end

local participantsSize = redis.call('SCARD', participantsKey)
if participantsSize == nil or participantsSize <= 0 then
  return {}
end

local pickCount = winnerCount
if pickCount > participantsSize then
  pickCount = participantsSize
end

local sampled = redis.call('SRANDMEMBER', participantsKey, pickCount)
if sampled == false or sampled == nil then
  return {}
end

if type(sampled) == 'string' then
  sampled = { sampled }
end

local winners = {}
for _, userId in ipairs(sampled) do
  if userId ~= false and userId ~= nil then
    redis.call('SADD', winnerUsersKey, userId)
    table.insert(winners, userId)
  end
end

return winners
