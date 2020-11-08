package spring.cloud.product.hystrix.collapser;

import com.netflix.hystrix.HystrixCollapser;
import com.netflix.hystrix.HystrixCollapserKey;
import com.netflix.hystrix.HystrixCollapserProperties;
import com.netflix.hystrix.HystrixCommand;
import UserInfo;
import spring.cloud.product.facade.UserFacade;
import spring.cloud.product.hystrix.cmd.UserFindCommand;

import java.util.Collection;
import java.util.List;

public class UserHystrixCollapser extends HystrixCollapser<List<UserInfo>, UserInfo, Long> {

    private UserFacade userFacade = null; // 用户服务接口
    private Long id; // 单个参数

    public UserHystrixCollapser(UserFacade userFacade, Long id) {
        // 构建相关的参数
        super(Setter.withCollapserKey(HystrixCollapserKey.Factory.asKey("userGroup"))
                // 请求范围内（作用域）
                .andScope(Scope.REQUEST)
                // 配置属性
                .andCollapserPropertiesDefaults(HystrixCollapserProperties.Setter()
                        // 并且只收集50 ms时间戳内的请求
                        .withTimerDelayInMilliseconds(50)
                        // 最多收集3次请求
                        .withMaxRequestsInBatch(3)));

        this.userFacade = userFacade;
        this.id = id;
    }

    @Override
    public Long getRequestArgument() { // 返回单次请求参数
        return id;
    }

    // 创建合并请求Hystrix命令
    @Override
    public HystrixCommand<List<UserInfo>> createCommand(
            Collection<CollapsedRequest<UserInfo, Long>> requests) {
        // 合并请求参数
        Long[] idArr = new Long[requests.size()];
        int index = 0;
        for (CollapsedRequest<UserInfo, Long> request : requests) {
            idArr[index] = request.getArgument();
            index++;
        }
        UserFindCommand ufc = new UserFindCommand(userFacade, idArr);
        return ufc;
    }

    @Override
    public void mapResponseToRequests(List<UserInfo> batchResponse,
                                      Collection<CollapsedRequest<UserInfo, Long>> requests) {
        int idx = 0; // 下标
        for (CollapsedRequest<UserInfo, Long> request : requests) {
            //将结果分发到各个单次请求中
            request.setResponse(batchResponse.get(idx));
            idx++;
        }
    }
}
