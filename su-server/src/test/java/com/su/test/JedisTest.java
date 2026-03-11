package com.su.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import redis.clients.jedis.Jedis;

@EnabledIfSystemProperty(named = "it.redis.enabled", matches = "true")
public class JedisTest {
    private Jedis jedis;

    @BeforeEach
    public void init() {
        //鍒涘缓jedis瀵硅薄
        jedis = new Jedis("192.168.100.128", 6379);
        //璁剧疆瀵嗙爜
        jedis.auth("123456");
        //閫夋嫨鏁版嵁搴?
        jedis.select(1);
    }
    @Test
    public void testJedis() {
        String set = jedis.set("name", "sky");
        System.out.println(set);
        String name = jedis.get("name");
        System.out.println(name);
    }

    @Test
    public void testHash(){
        jedis.hset("user:1","name","sky");
        String name = jedis.hget("user:1", "name");
        System.out.println(name);

    }

    @AfterEach
    public void destroy() {
        //鍏抽棴jedis
       if (jedis != null) jedis.close();
    }
}
