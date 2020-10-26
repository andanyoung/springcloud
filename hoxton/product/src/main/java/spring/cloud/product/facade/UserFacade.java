package spring.cloud.product.facade;

import rx.Observable;
import spring.cloud.common.vo.ResultMessage;

import java.util.List;

public interface UserFacade {

    public ResultMessage timeout();

    public ResultMessage timeout2();

    public ResultMessage exp(String msg);

    public List<ResultMessage> exp2(String[] params);

    public Observable<ResultMessage> asyncExp(String[] params);

    public ResultMessage fallback3(String[] params);
}
