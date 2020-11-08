package spring.cloud.zuul.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import spring.cloud.common.vo.ResultMessage;

@Component
// 如果ZuulFilter的子类被装配为Spring Bean，那么会自动注册为Zuul过滤器 ①
public class ValidateCodeFilter extends ZuulFilter {

    // 验证码键和值的参数名称
    private final static String VALIDATE_KEY_PARAM_NAME = "validateKey";
    private final static String VALIDATE_CODE_PARAM_NAME = "validateCode";
    // 注入StringRedisTemplate对象，这个对象由Spring Boot自动装配
    @Autowired
    private StringRedisTemplate strRedisTemplate = null;

    @Override
    public String filterType() { // 过滤器类型 “pre” ②
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public boolean shouldFilter() { //是否执行过滤器逻辑 ③
        // 获取请求上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        if (ctx.getRequestQueryParams() == null) { // 如果没有参数，不过滤
            return false;
        }
        // 是否存在对应的参数
        return ctx.getRequestQueryParams()
                .containsKey(VALIDATE_CODE_PARAM_NAME)
                && ctx.getRequestQueryParams()
                .containsKey(VALIDATE_KEY_PARAM_NAME);
    }

    @Override
    public int filterOrder() { // 过滤器的顺序 ⑥
        return FilterConstants.PRE_DECORATION_FILTER_ORDER + 15;
    }

    @Override
    public Object run() throws ZuulException { // 过滤器逻辑 ④// 获取请求上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        // 获取请求参数验证key
        String validateKey
                = ctx.getRequest().getParameter(VALIDATE_KEY_PARAM_NAME);
        // 请求参数验证码
        String validateCode
                = ctx.getRequest().getParameter(VALIDATE_CODE_PARAM_NAME);
        // Redis缓存的验证码
        String redisValidateCode
                = strRedisTemplate.opsForValue().get(validateKey);
        // 如果两个验证码相同，就放行
        if (validateCode.equals(redisValidateCode)) {
            return null;// 放行
        }
        // 不再放行路由，逻辑到此为止
        ctx.setSendZuulResponse(false);// ⑤
        // 设置响应码为401-未签名
        ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
        // 设置响应类型
        ctx.getResponse()
                .setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        // 响应结果
        ResultMessage result
                = new ResultMessage(false, "验证码错误，请检查您的输入");
        // 将result转换为JSON字符串
        ObjectMapper mapper = new ObjectMapper();
        String body = null;
        try {
            body = mapper.writeValueAsString(result); // 转变为JSON字符串
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        // 设置响应体
        ctx.setResponseBody(body);
        return null;
    }
}
