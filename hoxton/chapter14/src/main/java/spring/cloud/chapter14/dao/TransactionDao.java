package spring.cloud.chapter14.dao;

import org.apache.ibatis.annotations.Mapper;
import spring.cloud.chapter14.pojo.Transaction;

import java.util.List;

@Mapper
public interface TransactionDao {
    /**
     * 根据用户编号（userId）查找交易
     *
     * @param userId -- 用户编号
     * @return 交易信息
     */
    public List<Transaction> findTranctions(Long userId);
}
