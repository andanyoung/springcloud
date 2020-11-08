package spring.cloud.fund.fallback;

import feign.hystrix.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import spring.cloud.common.pojo.UserInfo;
import spring.cloud.common.vo.ResultMessage;
import spring.cloud.fund.facade.UserFacade;

import java.util.List;

/**
 * 定义OpenFeign降级工厂（FallbackFactory）分3步：
 * 1. 实现接口FallbackFactory<T>定义的create方法
 * 2. 将降级工厂定义为一个Spring Bean
 * 3. 使用@FeignClient的fallbackFactory配置项定义
 */
@Component  // ①
public class UserFallbackFactory implements FallbackFactory<UserFacade> {

    /**
     * 通过FallbackFactory接口定义的create方法参数获取异常信息
     */
    @Override
    public UserFacade create(Throwable err) {
        // 返回一个OpenFeign接口的实现类
        return new UserFacade() {
            // 错误编号
            private Long ERROR_ID = Long.MAX_VALUE;

            @Override
            public UserInfo getUser(Long id) {
                return new UserInfo(ERROR_ID, null, err.getMessage());
            }

            @Override
            public UserInfo putUser(UserInfo userInfo) {
                return new UserInfo(ERROR_ID, null, err.getMessage());
            }

            @Override
            public ResponseEntity<List<UserInfo>> findUsers2(
                    // @RequestParam代表请求参数
                    @RequestParam("ids") Long[] ids) {
                return null;
            }

            @Override
            public ResultMessage deleteUser(Long id) {
                return new ResultMessage(false, err.getMessage());
            }

            @Override
            public ResultMessage uploadFile(MultipartFile file) {
                return new ResultMessage(false, err.getMessage());
            }

        };
    }
}
