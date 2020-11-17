package spring.cloud.chapter14.type.handler;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;
import spring.cloud.chapter14.enumeration.PaymentChannelEnum;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// 定义需要转换的Java类型
@MappedTypes(PaymentChannelEnum.class)
// 定义需要转换的Jdbc类型
@MappedJdbcTypes(JdbcType.INTEGER) // ①
public class PaymentChannelHandler implements TypeHandler<PaymentChannelEnum> {// ②

    @Override
    public void setParameter(PreparedStatement preparedStatement, int i, PaymentChannelEnum paymentChannelEnum, JdbcType jdbcType) throws SQLException {
        preparedStatement.setInt(i, paymentChannelEnum.getId());
    }

    @Override
    public PaymentChannelEnum getResult(ResultSet resultSet, String s) throws SQLException {
        int anInt = resultSet.getInt(s);
        return PaymentChannelEnum.getById(anInt);
    }

    @Override
    public PaymentChannelEnum getResult(ResultSet resultSet, int i) throws SQLException {
        int anInt = resultSet.getInt(i);
        return PaymentChannelEnum.getById(anInt);
    }

    @Override
    public PaymentChannelEnum getResult(CallableStatement callableStatement, int i) throws SQLException {
        int anInt = callableStatement.getInt(i);
        return PaymentChannelEnum.getById(anInt);
    }
}
