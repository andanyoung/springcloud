package spring.cloud.fund;

import okhttp3.OkHttpClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
// 在新版本的 Spring Cloud中 ，不再需要这个注解驱动服务发现了
//@EnableDiscoveryClient
@EnableFeignClients(
        // 扫描装配OpenFeign接口到IoC容器中
        basePackages = "spring.cloud.fund"
)
public class FundApplication {

    @Bean
    public OkHttpClient.Builder okHttpClientBuilder() {
        return new OkHttpClient.Builder()
                // 读取超时时间（不包含解析地址，提交请求的耗时）
                .readTimeout(2, TimeUnit.SECONDS)
                // 写入超时时间
                .writeTimeout(5, TimeUnit.SECONDS)
                // 连接远程服务器超时时间
                .connectTimeout(3, TimeUnit.SECONDS)
                // 如果连接远程服务器失败是否重试
                .retryOnConnectionFailure(true)
                // 当HTTP返回码为3xx（重定向）时，是否执行重定向操作
                .followRedirects(true);
    }

    public static void main(String[] args) {
        SpringApplication.run(FundApplication.class, args);
    }
}
