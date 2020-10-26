package spring.cloud.product.facade;

import spring.cloud.common.vo.ResultMessage;

public interface UserFacade {

    public ResultMessage timeout();

    public ResultMessage exp(String msg);

}
