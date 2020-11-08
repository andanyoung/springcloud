package spring.cloud.zuul.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import spring.cloud.common.vo.ResultMessage;

import java.util.concurrent.Callable;


@Component // 扫描过滤器 ①
public class RateLimiterFilter extends ZuulFilter {
    // 注入限速器注册机
    @Autowired
    private RateLimiterRegistry rateLimiterRegistry = null;

    // 对用户微服务的请求正则式匹配
    private static final String USER_PRE = "/u/";

    @Override
    public String filterType() { // “pre”类型过滤器
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() { // 过滤器顺序⑤
        return FilterConstants.PRE_DECORATION_FILTER_ORDER + 30;
    }

    /**
     * 过滤器是否拦截
     *
     * @return boolean，true拦截，false不拦截
     */
    @Override
    public boolean shouldFilter() { // 只是限制路由用户微服务的请求②
        // 获取请求上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        // 获取URI
        String uri = ctx.getRequest().getRequestURI();
        // 判定请求路径是否为转发用户微服务
        return uri.startsWith(USER_PRE);
    }

    @Override
    public Object run() throws ZuulException {
        // 获取Resilience4j限速器
        RateLimiter userRateLimiter = rateLimiterRegistry.rateLimiter("user");
        // 限速器逻辑
        Callable<ResultMessage> call1 = () -> new ResultMessage(true, "通过");
        // 绑定限速器
        Callable<ResultMessage> call2
                = RateLimiter.decorateCallable(userRateLimiter, call1); // 尝试获取结果
        Try<ResultMessage> tryResult = Try.of(() -> call2.call())
                // ③
                // 降级逻辑
                .recover(ex -> new ResultMessage(false, "超过所限流量"));
        ResultMessage result = tryResult.get();
        if (result.getSuccess()) { // 如果成功则在限流范围内，放行服务
            return null;
        }
        /** 以下为超过流量的处理 **/
        // 获取请求上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        // 不再进行下一步的路由，而是到此为止 ctx.setSendZuulResponse(false); // ④
        // 设置响应码为400-坏请求
        ctx.setResponseStatusCode(HttpStatus.TOO_MANY_REQUESTS.value());
        // 设置响应类型
        ctx.getResponse()
                .setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        // 转换为JSON字符串
        ObjectMapper mapper = new ObjectMapper();
        String body = null;
        try {
            body = mapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        // 设置响应体
        ctx.setResponseBody(body);
        return null;
    }
}
