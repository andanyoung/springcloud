package spring.cloud.chapter14.pojo;

import lombok.Data;
import org.apache.ibatis.type.Alias;
import spring.cloud.chapter14.enumeration.PaymentChannelEnum;

import java.io.Serializable;
import java.util.Date;

@Alias("transaction")
@Data
public class Transaction implements Serializable {
    public static final long serialVersionUID = 2323902389475832678L;
    private Long id;
    private Long userId;
    private Long productId;
    private PaymentChannelEnum paymentChannel = null; // 枚举
    private Date transDate;
    private Double amout;
    private Integer quantity;
    private Double discount;
    private String note;
}
