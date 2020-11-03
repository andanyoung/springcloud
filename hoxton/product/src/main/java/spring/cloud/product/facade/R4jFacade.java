package spring.cloud.product.facade;

import spring.cloud.common.vo.ResultMessage;
import spring.cloud.common.vo.UserInfo;

public interface R4jFacade {
    public ResultMessage exp(String msg);

    public UserInfo getUser(Long id);

    public UserInfo getUser3(Long id);

    public ResultMessage exp();

    public UserInfo cacheUserInfo(Long id);

    public ResultMessage timeout();

    public UserInfo mixUserInfo(Long id);
}
