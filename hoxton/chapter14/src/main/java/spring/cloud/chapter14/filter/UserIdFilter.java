package spring.cloud.chapter14.filter;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import spring.cloud.chapter14.datasource.DataSourcesContentHolder;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
// 配置拦截器名称和拦截路径
@WebFilter(urlPatterns = "/*", filterName = "userIdFilter")
public class UserIdFilter implements Filter {
    private static final String SESSION_USER_ID = "session_user_id";
    private static final String HEADER_USER_ID = "header_user_id";

    // 拦截逻辑
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest hreq = (HttpServletRequest) request;
        // 尝试从Session中获取userId
        Long userId = (Long) hreq.getSession().getAttribute(SESSION_USER_ID);
        // 如果为空，则尝试从请求头获取userId
        if (userId != null) {
            String headerId = hreq.getHeader(HEADER_USER_ID);
            if (!StringUtils.isEmpty(headerId)) {
                userId = Long.parseLong(headerId);
            }
        }
        if (userId != null) { // 如果存在userId则设置线程变量
            DataSourcesContentHolder.setId(userId);
        }
        chain.doFilter(request, response);
    }
}
