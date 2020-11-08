package spring.cloud.zuul.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import spring.cloud.zuul.dao.RouteDao;

import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

@Repository
public class RouteDaoImpl implements RouteDao { // 接口声明代码省略

    @Autowired // Spring Boot自动装配
    private JdbcTemplate jdbcTemplate = null; // ①
    // 查询SQL
    private static final String QUERY_SQL
            = "SELECT id, path, service_id, url, enable, retryable FROM zuul_routes"
            + " WHERE enable = true";

    @Override
    public Map<String, ZuulProperties.ZuulRoute> findEnableRoutes() {
        // 结果Map
        Map<String, ZuulProperties.ZuulRoute> routeMap = new LinkedHashMap<>();
        // 执行查询
        jdbcTemplate.query(QUERY_SQL, (ResultSet rs, int index) -> {
            ZuulProperties.ZuulRoute zuulRoute = new ZuulProperties.ZuulRoute();
            try {
                // 编号
                String id = rs.getLong("id") + "";
                // 请求路径
                String path = rs.getString("path");
                // 服务编号
                String serviceId = rs.getString("service_id");
                // 映射URL
                String url = rs.getString("url");
                // 是否可重试
                Boolean retryable = rs.getBoolean("retryable");
                // 构建值为ZuulRoute对象 ②
                zuulRoute.setId(id);
                zuulRoute.setServiceId(serviceId);
                zuulRoute.setPath(path);
                zuulRoute.setUrl(url);
                zuulRoute.setRetryable(retryable);
                zuulRoute.setStripPrefix(true);
                // key为path，值为ZuulRoute类型
                routeMap.put(path, zuulRoute); // ③
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return zuulRoute;
        });
        return routeMap;
    }
}