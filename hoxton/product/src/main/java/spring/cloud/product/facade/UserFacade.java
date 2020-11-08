package spring.cloud.product.facade;

import UserInfo;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheKey;
import rx.Observable;
import spring.cloud.common.vo.ResultMessage;

import java.util.List;
import java.util.concurrent.Future;

public interface UserFacade {

    public ResultMessage timeout();

    public ResultMessage timeout2();

    public ResultMessage exp(String msg);

    public List<ResultMessage> exp2(String[] params);

    public Observable<ResultMessage> asyncExp(String[] params);

    public ResultMessage fallback3(String[] params);

    public ResultMessage fallback2(String msg, Throwable ex);

    public UserInfo testUserInfo(Long id);

    public UserInfo getUserInfo(@CacheKey Long id);

    public UserInfo updateUserInfo(@CacheKey("id") UserInfo user);

    public UserInfo getUserInfo2(@CacheKey Long id);

    public UserInfo updateUserInfo2(UserInfo user);

    public UserInfo getUser(Long id);

    public List<UserInfo> findUsers(Long[] ids);


    public Future<UserInfo> getUser2(Long id);

    public List<UserInfo> findUsers2(List<Long> ids);
}
