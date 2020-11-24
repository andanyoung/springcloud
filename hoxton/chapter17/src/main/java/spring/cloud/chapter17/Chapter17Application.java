package spring.cloud.chapter17;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
// 启动使用Spring Session Redis
// maxInactiveIntervalInSeconds配置项默认为30分钟（1800秒）失效，
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600)
@RestController
@RequestMapping("/session")
public class Chapter17Application {

    public static void main(String[] args) {
        SpringApplication.run(Chapter17Application.class, args);
    }

    // 写入测试
    @GetMapping("/set/{key}/{value}")
    public Map<String, String> setSessionAtrribute(HttpServletRequest request,
                                                   @PathVariable("key") String key, @PathVariable("value") String value) {
        Map<String, String> result = new HashMap<>();
        result.put(key, value);
        request.getSession().setAttribute(key, value);
        return result;
    }

    // 读出测试
    @GetMapping("/get/{key}")
    public Map<String, String> getSessionAtrribute(HttpServletRequest request,
                                                   @PathVariable("key") String key) {
        Map<String, String> result = new HashMap<>();
        String value = (String) request.getSession().getAttribute(key);
        result.put(key, value);
        request.getSession().setAttribute(key, value);
        return result;
    }
}
