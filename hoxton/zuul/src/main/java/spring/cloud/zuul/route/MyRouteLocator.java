package spring.cloud.zuul.route;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.discovery.DiscoveryClientRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.discovery.ServiceRouteMapper;
import spring.cloud.zuul.dao.RouteDao;

import java.util.LinkedHashMap;
import java.util.Map;

/**** imports ****/
public class MyRouteLocator extends DiscoveryClientRouteLocator {

    @Autowired
    private RouteDao routeDao = null;

    // 构造方法
    public MyRouteLocator(String servletPath, DiscoveryClient discovery,
                          ZuulProperties properties,
                          ServiceRouteMapper serviceRouteMapper,
                          ServiceInstance localServiceInstance) {
        // 父构造方法 ①
        super(servletPath, discovery, properties,
                serviceRouteMapper, localServiceInstance);
    }

    @Override
    public LinkedHashMap<String, ZuulProperties.ZuulRoute> locateRoutes() {
        // 调用父类方法，加载静态配置的路由规则
        LinkedHashMap<String, ZuulProperties.ZuulRoute> resultMap
                = super.locateRoutes(); // ②
        // 加载数据库配置的路由规则
        Map<String, ZuulProperties.ZuulRoute> dbMap
                = routeDao.findEnableRoutes();
        resultMap.putAll(dbMap); // ③
        return resultMap;
    }
}