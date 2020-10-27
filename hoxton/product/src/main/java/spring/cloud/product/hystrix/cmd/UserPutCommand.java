package spring.cloud.product.hystrix.cmd;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import spring.cloud.common.vo.UserInfo;

public class UserPutCommand extends HystrixCommand<UserInfo> {

    private UserInfo user = null; // 用户信息
    private RestTemplate restTemplate = null; // REST模板
    // 请求URL
    private final String URL = "http://USER/user/info";

    public UserPutCommand(RestTemplate restTemplate,
                          Long id, String userName, String note) {
        super(Setter.withGroupKey(
                HystrixCommandGroupKey.Factory.asKey("userGroup")));
        this.restTemplate = restTemplate;
        // 创建用户信息对象
        user = new UserInfo(id, userName, note);
    }

    @Override
    public UserInfo run() throws Exception {
        // 请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        // 封装请求实体对象，将用户信息对象设置为请求体
        HttpEntity<UserInfo> request = new HttpEntity<>(user, headers);
        System.out.println("执行更新用户" + user.getId());
        // 更新用户信息
        restTemplate.put(URL, request);
        // 清除缓存
        UserGetCommand.clearCache(user.getId());
        return user;
    }
}

