package spring.cloud.chapter15.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

@Service
public class XaService {
    @Autowired
    @Qualifier("jdbcTmpl1")
    private JdbcTemplate jdbcTmpl1 = null;

    @Autowired
    @Qualifier("jdbcTmpl2")
    private JdbcTemplate jdbcTmpl2 = null;

    // 注入数据库事务管理器
    @Autowired
    PlatformTransactionManager transactionManager; // ①

    @Transactional // 开启事务
    public int inisertFoo(Long id, String content, Long id2, String content2) {
        // 查看异常类型
        System.out.println("数据库事务管理器类型："
                + transactionManager.getClass().getName());
        int count = 0;
        String sql = "insert into t_foo(id, content) values(?, ?)";
        count += jdbcTmpl1.update(sql, id, content); // ②
        // 测试异常时，可以设置id2为null，进行验证
        count += jdbcTmpl2.update(sql, id2, content2); // ③
        return count;
    }


}
