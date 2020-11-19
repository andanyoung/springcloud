package spring.cloud.chapter15.params;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FundParams implements Serializable {

    public static final long serialVersionUID = 989878441231256478L;
    private Long xid; // 业务流水号
    private Long userId; // 用户编号
    private Double amount; // 交易金额
}
