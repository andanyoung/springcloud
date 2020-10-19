package spring.cloud.product.config;

import com.netflix.discovery.endpoint.EndpointUtils;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerListFilter;
import org.springframework.cloud.netflix.ribbon.ZonePreferenceServerListFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlobalConfiguration {

    // Bean Name和表4-1保持一致
    // 服务过滤器
    @Bean(name = "ribbonServerListFilter")
    public ServerListFilter<Server> serverListFilter() {
        // 使用优先选择的过滤器
        ZonePreferenceServerListFilter filter
                = new ZonePreferenceServerListFilter();
        // 使用默认Zone
        filter.setZone(EndpointUtils.DEFAULT_ZONE);
        return filter;
    }

    // 负载均衡策略
    @Bean
    public IRule rule() {
        // 使用随机选择服务的策略
        return new RandomRule();
    }

}
