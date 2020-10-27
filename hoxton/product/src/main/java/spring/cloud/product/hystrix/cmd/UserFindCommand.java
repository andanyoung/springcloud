package spring.cloud.product.hystrix.cmd;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import spring.cloud.common.vo.UserInfo;
import spring.cloud.product.facade.UserFacade;

import java.util.List;

public class UserFindCommand extends HystrixCommand<List<UserInfo>> {

    private Long[] ids = null; // 参数
    private UserFacade userFacade = null;

    public UserFindCommand(UserFacade userFacade, Long[] ids) {
        // 在当前的命令中加入命令Key
        super(HystrixCommand.Setter.withGroupKey(
                HystrixCommandGroupKey.Factory.asKey("userGroup")));
        this.userFacade = userFacade;
        this.ids = ids;
    }

    // 调用接口查询用户
    @SuppressWarnings("unchecked")
    @Override
    protected List<UserInfo> run() throws Exception {
        List<UserInfo> userList = userFacade.findUsers(ids);
        return userList;
    }
}
