package spring.cloud.chapter15;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.mysql.cj.jdbc.MysqlXADataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@SpringBootApplication
public class Chapter15Application {

    public static void main(String[] args) {
        SpringApplication.run(Chapter15Application.class, args);
    }

    /**
     * 定义数据源1
     *
     * @return 数据源1
     */
    @Primary // 如果遇到DataSource注入，该Bean拥有优先注入权
    @Bean("ds1")
    public DataSource dataSource1() {
        // Atomikos 提供的数据源，可以帮助我们设置数据库连接池属性
        AtomikosDataSourceBean ds = new AtomikosDataSourceBean(); // ①
        // MySQL的XA协议数据源
        MysqlXADataSource xaDs = new MysqlXADataSource(); // ②
        // 设置数据库连接属性
        xaDs.setUrl("jdbc:mysql://localhost:3306/spring_cloud_chapter15_1" +
                "?serverTimezone=UTC");
        xaDs.setUser("root");
        xaDs.setPassword("123456");
        // Atomikos数据源绑定MySQL的XA协议数据源
        ds.setXaDataSource(xaDs); // 设置Atomikos数据源的唯一标识名
        ds.setUniqueResourceName("ds1"); // ③
        initPool(ds);
        return ds;
    }

    @Bean("ds2")
    public DataSource dataSource2() {
        AtomikosDataSourceBean ds = new AtomikosDataSourceBean();
        MysqlXADataSource xaDs = new MysqlXADataSource();
        xaDs.setUrl("jdbc:mysql://localhost:3306/spring_cloud_chapter15_2" +
                "?serverTimezone=UTC");
        xaDs.setUser("root");
        xaDs.setPassword("123456");
        ds.setXaDataSource(xaDs);
        ds.setUniqueResourceName("ds2");
        initPool(ds);
        return ds;
    }

    /**
     * 设置数据库连接池的属性
     *
     * @param ds -- AtomikosDataSourceBean数据源
     */
    private void initPool(AtomikosDataSourceBean ds) {
        // 连接池最大连接数
        ds.setMaxPoolSize(50);
        // 连接池最小连接数
        ds.setMinPoolSize(10);
        // 连接池默认连接数
        ds.setPoolSize(30);
    }

    /**
     * 创建优先注入的JdbcTemplate对象
     *
     * @param ds -- 数据源，将使用@Qualifier("ds1")限定所绑定的数据库
     * @return JdbcTemplate对象
     */
    @Primary
    @Bean("jdbcTmpl1")
    public JdbcTemplate jdbcTemplate1(@Qualifier("ds1") DataSource ds) {
        JdbcTemplate jdbcTmpl = new JdbcTemplate();
        // 绑定数据源
        jdbcTmpl.setDataSource(ds);
        return jdbcTmpl;
    }

    @Bean("jdbcTmpl2")
    public JdbcTemplate jdbcTemplate2(@Qualifier("ds2") DataSource ds) {
        JdbcTemplate jdbcTmpl = new JdbcTemplate();
        jdbcTmpl.setDataSource(ds);
        return jdbcTmpl;
    }
}
