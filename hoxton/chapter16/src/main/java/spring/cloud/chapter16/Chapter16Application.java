package spring.cloud.chapter16;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@RestController
@RequestMapping("/redis")
public class Chapter16Application {

    public static void main(String[] args) {
        SpringApplication.run(Chapter16Application.class, args);
    }

    // 注入StringRedisTemplate对象，该对象操作字符串，由Spring Boot机制自动装配
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 测试Redis写入
    @GetMapping("/write")
    public Map<String, String> testWrite() {
        Map<String, String> result = new HashMap<>();
        result.put("key1", "value1");
        stringRedisTemplate.opsForValue().multiSet(result);
        return result;
    }

    // 测试Redis读出
    @GetMapping("/read")
    public Map<String, String> testRead() {
        Map<String, String> result = new HashMap<>();
        result.put("key1", stringRedisTemplate.opsForValue().get("key1"));
        return result;
    }

    // ShardedJedis 连接池
    private ShardedJedisPool pool = null;

    @Bean
    public ShardedJedisPool initJedisPool() {
        // 端口数组
        int[] ports = {7001, 7002, 7003};
        // 权重数组
        int[] weights = {1, 2, 1};
        // 服务器
        String host = "192.168.224.136";
        // 密码
        String password = "123456";
        // 连接超时时间
        int connectionTimeout = 2000;
        // 读超时时间
        int soTimeout = 2000;
        List<JedisShardInfo> shardList = new ArrayList<>();
        for (int i = 0; i < ports.length; i++) {
            // 创建JedisShard信息
            JedisShardInfo shard = new JedisShardInfo(host, ports[i], connectionTimeout, soTimeout, weights[i]); //①
            // 设置密码
            shard.setPassword(password);
            // 加入到列表中
            shardList.add(shard);
        }
        // 连接池配置
        JedisPoolConfig poolCfg = new JedisPoolConfig();
        poolCfg.setMaxIdle(10);
        poolCfg.setMinIdle(5);
        poolCfg.setMaxIdle(10);
        poolCfg.setMaxTotal(30);
        poolCfg.setMaxWaitMillis(2000); // 创建ShardedJedis连接池 pool = new ShardedJedisPool(poolCfg, shardList); // ②
        return pool;
    }

    // 测试Redis写入
    @GetMapping("/test2")
    public Map<String, String> test2() {
        Map<String, String> result = new HashMap<>();
        ShardedJedis jedis = null;
        try {
            // 获得ShardedJedis对象 ①
            jedis = pool.getResource();  // 写入Redis
            jedis.set("key1", "value1");
            // 从Redis读出
            result.put("key1", jedis.get("key1"));
            return result;
        } finally {
            // 最后释放连接
            jedis.close(); // ②
        }
    }
}
