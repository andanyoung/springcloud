package spring.cloud.chapter15.service;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import spring.cloud.chapter15.params.FundParams;
import spring.cloud.chapter15.utils.SnowFlakeWorker;

@Service
public class PurchaseService implements RabbitTemplate.ConfirmCallback { // ①

    // SnowFlake算法生成ID
    SnowFlakeWorker worker = new SnowFlakeWorker(003);
    // RabbitMQ模板
    @Autowired
    private RabbitTemplate rabbitTemplate;
    // 读取配置属性
    @Value("${rabbitmq.queue.fund}")
    private String fundQueueName = null;

    // 购买业务方法
    public Long purchase(Long productId, Long userId, Double amount) {
        rabbitTemplate.setConfirmCallback(this); // ②
        // SnowFlake算法生成序列号，业务通过它在各个服务间进行关联
        Long xid = worker.nextId(); // ③
        // 传递给消费者的参数
        FundParams params = new FundParams(xid, userId, amount);
        // 发送消息给资金服务做扣款
        this.rabbitTemplate.convertAndSend(fundQueueName, params); // ④
        System.out.println("执行产品服务逻辑");
        return xid;
    }


    /**
     * 确认回调，会异步执行
     *
     * @param correlationData -- 相关数据
     * @param ack             -- 是否被消费
     * @param cause           -- 失败原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) { // 消息投递成功
            System.out.println("执行交易成功");
        } else { // 消息投递失败
            try {
                // 停滞1秒（稍微等待可能没有完成的正常流程），然后发起冲正交易
                Thread.sleep(1000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.out.println("尝试产品减库存冲正交易。");
            System.out.println("尝试账户扣减冲正交易。");
            System.out.println(cause); // 打印消息投递失败的原因
        }
    }
}
