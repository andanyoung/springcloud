package spring.cloud.zuul;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.discovery.ServiceRouteMapper;

@SpringBootApplication(scanBasePackages = "spring.cloud.zuul")
// 驱动Zuul代理启动
@EnableZuulProxy
public class ZuulApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZuulApplication.class, args);
    }


    // Zuul配置
    @Autowired
    protected ZuulProperties zuulProperties;

    // 服务器配置
    @Autowired
    protected ServerProperties server;

    // 服务注册表
    @Autowired(required = false)
    private Registration registration;

    // 服务发现客户端
    @Autowired
    private DiscoveryClient discovery;

    // 服务路由映射器
    @Autowired
    private ServiceRouteMapper serviceRouteMapper;

//    @Bean(name = "routeLocator")
//    public MyRouteLocator initMyRouteLocator() {
//        return new MyRouteLocator(
//                this.server.getServlet().getContextPath(),
//                this.discovery,
//                this.zuulProperties,
//                this.serviceRouteMapper,
//                this.registration);
//    }

}
