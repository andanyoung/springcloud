package spring.cloud.chapter14.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import spring.cloud.chapter14.dao.TransactionDao;
import spring.cloud.chapter14.datasource.DataSourcesContentHolder;
import spring.cloud.chapter14.pojo.Transaction;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class TransactionController {
    @Resource
    private TransactionDao transactionDao;

    @GetMapping("/transactions/{userId}")
    public List<Transaction> findTransaction(
            @PathVariable("userId") Long userId) {
        // 设置用户编号，这样就能够根据规则找到具体的数据库
        DataSourcesContentHolder.setId(userId);
        return transactionDao.findTranctions(userId);
    }
}
