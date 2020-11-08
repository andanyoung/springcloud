package spring.cloud.zuul.dao;

import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

import java.util.Map;

public interface RouteDao {

    public Map<String, ZuulProperties.ZuulRoute> findEnableRoutes();
}
