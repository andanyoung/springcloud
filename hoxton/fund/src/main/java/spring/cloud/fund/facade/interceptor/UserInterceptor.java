package spring.cloud.fund.facade.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class UserInterceptor implements RequestInterceptor {
    /**
     * 拦截器的意义在于，根据自己的需要定制RestTemplate和请求参数、请求体等
     *
     * @param template -- 请求模板
     */

    @Override
    public void apply(RequestTemplate template) {
        // 这里只是随意给出一个请求头参数，实践中一般可以传递token参数等
        template.header("id", "1");
    }
}
