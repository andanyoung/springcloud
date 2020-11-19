package spring.cloud.chapter15.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {
    // 读取配置属性
    @Value("${rabbitmq.queue.fund}")
    private String fundQueueName;

    // 创建RabbitMQ消息队列
    @Bean(name = "fundQueue")
    public Queue createFundQueue() {
        return new Queue(fundQueueName);
    }
}
