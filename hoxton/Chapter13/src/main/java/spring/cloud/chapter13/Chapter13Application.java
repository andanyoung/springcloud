package spring.cloud.chapter13;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@RestController
@RequestMapping("/chapter13")
public class Chapter13Application {

    public static void main(String[] args) {
        SpringApplication.run(Chapter13Application.class, args);
    }

    private String lua = // ①

            // 获取是否启用标志
            " local start = redis.call('hget', KEYS[1], 'start') \n"
                    // 如果未启用
                    + " if tonumber(start) == 0 then \n" // 获取开始值
                    + " local result = redis.call('hget', KEYS[1], 'offset') \n" // 将当前值设置为开始值
                    + " redis.call('hset', KEYS[1], 'current', result) \n" // 将是否启用标志设置为已经启用（1）
                    + " redis.call('hset', KEYS[1], 'start', '1') \n" // 返回结束
                    + " return result \n" // 结束if语句
                    + " end \n" // 获取当前值
                    + " local current = redis.call('hget', KEYS[1], 'current') \n" // 获取步长
                    + " local step = redis.call('hget', KEYS[1], 'step') \n" // 结算新的当前值
                    + " local result = current + step \n" // 设置新的当前值
                    + " redis.call('hset', KEYS[1], 'current', result) \n" // 返回结果
                    + " return result \n";

    @Autowired
    private StringRedisTemplate stringRedisTemplate = null;

    /**
     * 获取对应的ID
     *
     * @param keyType -- Redis的键
     * @return ID
     */
    @GetMapping("/id/{keyType}")
    public String getKey(@PathVariable("keyType") String keyType) { // ②
        // 结果返回为Long
        DefaultRedisScript<Long> rs = new DefaultRedisScript<Long>();
        rs.setScriptText(lua);
        rs.setResultType(Long.class);
        // 定义脚本中的key参数
        List<String> keyList = new ArrayList<>();
        keyList.add(keyType);
        // 执行脚本，并传递参数
        Object result = stringRedisTemplate.execute(rs, keyList);
        return result.toString();
    }

    /**
     * 获取对应的ID， 时钟算法
     *
     * @param keyType -- Redis的键
     * @return ID
     */
    @GetMapping("/clock")
    public String getKeyClock() {

        return String.valueOf(timeKey());
    }


    // 同步锁
    private static final Class<Chapter13Application> LOCK
            = Chapter13Application.class;

    public static long timeKey() {
        // 线程同步锁，防止多线程错误
        synchronized (LOCK) { // ①
            // 获取当前时间的纳秒值
            long result = System.nanoTime();
            // 死循环
            while (true) {
                long current = System.nanoTime();
                // 超过1 ns后才返回，这样便可保证当前时间肯定和返回的不同，
                // 从而达到排重的效果
                if (current - result > 1) { //
                    // 返回结果
                    return result;
                }
            }
        }
    }

}
