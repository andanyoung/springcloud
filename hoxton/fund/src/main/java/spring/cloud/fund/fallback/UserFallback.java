package spring.cloud.fund.fallback;


import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import spring.cloud.common.pojo.UserInfo;
import spring.cloud.common.vo.ResultMessage;
import spring.cloud.fund.facade.UserFacade;

import java.util.List;

/**
 * 要使类提供降级方法，需要满足3个条件：
 * 1. 实现OpenFeign接口定义的方法
 * 2. 将Bean注册为Spring Bean
 * 3. 使用@FeignClient的fallback配置项指向当前类
 *
 * @author ykzhen
 */
@Component // 注册为Spring Bean ①
public class UserFallback implements UserFacade {
    @Override
    public UserInfo getUser(Long id) {
        return new UserInfo(null, null, null);
    }

    @Override
    public UserInfo putUser(UserInfo userInfo) {
        return new UserInfo(null, null, null);
    }

    @Override
    public ResponseEntity<List<UserInfo>> findUsers2(
            // @RequestParam代表请求参数
            @RequestParam("ids") Long[] ids) {
        return null;
    }

    @Override
    public ResultMessage deleteUser(Long id) {
        return new ResultMessage(false, "降级服务");
    }


    @Override
    public ResultMessage uploadFile(MultipartFile file) {
        return new ResultMessage(false, "降级服务");

    }
}
