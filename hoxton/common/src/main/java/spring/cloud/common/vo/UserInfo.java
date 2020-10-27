package spring.cloud.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo implements Serializable {
    public static final long serialVersionUID = 15213856L;

    private Long id;
    private String userName;
    private String note;
}

