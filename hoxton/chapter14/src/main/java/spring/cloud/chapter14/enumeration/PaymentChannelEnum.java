package spring.cloud.chapter14.enumeration;

public enum PaymentChannelEnum {
    BANK_CARD(1, "银行卡交易"),
    WE_CHAT(2, "银行卡交易"),
    ALI_PAY(3, "支付宝"),
    OTHERS(4, "其他方式");

    private Integer id;
    private String name;

    PaymentChannelEnum(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public static PaymentChannelEnum getById(Integer id) {
        for (PaymentChannelEnum type : PaymentChannelEnum.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        throw new RuntimeException(
                "没有找到对应的枚举，请检测id【" + id + "】");
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
