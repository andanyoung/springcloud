package spring.cloud.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import io.jmnarloch.spring.cloud.ribbon.support.RibbonFilterContextHolder;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

@Component
public class GrayReleaseZuulFilter extends ZuulFilter {

    // 灰色发布控制参数名称
    private static final String GRAY_PARAM = "gray-release";
    // 灰色发布启用标记
    private static final String GRAY_ENABLE = "1";
    // 灰色发布禁用标记
    private static final String GRAY_DISABLE = "0";

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER + 30;
    }

    // 判断是否为路由用户微服务
    @Override
    public boolean shouldFilter() { // ①
        RequestContext ctx = RequestContext.getCurrentContext();
        String uri = ctx.getRequest().getRequestURI();
        return uri.startsWith("/u/") || uri.startsWith("/user/")
                || uri.startsWith("/user-api");
    }

    @Override
    public Object run() throws ZuulException {
        // 获取请求上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        // 获取请求控制参数
        String grayHeader = ctx.getRequest().getParameter(GRAY_PARAM); // ②
        // 不存在请求参数或参数不为灰色发布标志，则只请求正常发布的服务实例
        if (StringUtils.isBlank(grayHeader)
                || !GRAY_ENABLE.equals(grayHeader)) { // 设置元数据过滤条件
            RibbonFilterContextHolder.getCurrentContext().add(GRAY_PARAM, GRAY_DISABLE); // ③
        } else {
            // 存在灰色发布参数，且参数有效
            // 设置元数据过滤条件
            RibbonFilterContextHolder.getCurrentContext().add(GRAY_PARAM, GRAY_ENABLE); // ④
        }
        return null;
    }
}
