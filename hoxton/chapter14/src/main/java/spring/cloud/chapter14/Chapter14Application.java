package spring.cloud.chapter14;

import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.StringUtils;
import spring.cloud.chapter14.datasource.DataSourcesContentHolder;

import javax.sql.DataSource;
import java.util.*;

@SpringBootApplication
@MapperScan( // 定义扫描MyBatis的映射接口 ①
        basePackages = "spring.cloud.chapter14.*",// 扫描
        annotationClass = Mapper.class // 限定扫描被@Mapper注解的接口
)
public class Chapter14Application {

    public static void main(String[] args) {
        SpringApplication.run(Chapter14Application.class, args);
    }

    // 环境上下文
    @Autowired
    private Environment env; // ②
    // 数据源id列表

    // 数据源id列表
    private List<String> keyList = new ArrayList<>();

    // 获取数据库连接池配置
    private Properties poolProps() { // ③   // 获取连接池参数
        Properties props = new Properties();
        props.setProperty("maxIdle", env.getProperty("jdbc.pool.max-idle"));
        props.setProperty("maxTotal",
                env.getProperty("jdbc.pool.max-active"));
        props.setProperty("minIdle", env.getProperty("jdbc.pool.min-idle"));
        return props;
    }


    // 初始化单个数据源
    private DataSource initDataSource(Properties props, int idx)
            throws Exception {
        // 读入配置属性
        String url = env.getProperty("jdbc.ds" + idx + ".url");
        String username = env.getProperty("jdbc.ds" + idx + ".username");
        String password = env.getProperty("jdbc.ds" + idx + ".password");
        String driverClassName
                = env.getProperty("jdbc.ds" + idx + ".driverClassName");
        // 设置属性
        props.setProperty("url", url);
        props.setProperty("username", username);
        props.setProperty("password", password);
        props.setProperty("driverClassName", driverClassName);
        // 使用事务方式
        props.setProperty("defaultAutoCommit", "false"); // 创建数据源 ④
        return BasicDataSourceFactory.createDataSource(props);
    }

    // 初始化多数据源
    @Bean
    public AbstractRoutingDataSource initMultiDataSources() throws Exception {
        // 创建多数据源
        AbstractRoutingDataSource ds = new AbstractRoutingDataSource() { // ⑤
            @Override
            protected Object determineCurrentLookupKey() {// 获取线程副本中的变量值
                Long id = DataSourcesContentHolder.getId(); // ⑥
                // 求模算法
                Long idx = id % keyList.size();
                return keyList.get(idx.intValue());
            }
        };

        // 获取连接池属性
        Properties props = poolProps();
        int count = 1;
        Map<Object, Object> targetDs = new HashMap<>();
        do {
            // 获取id
            String id = env.getProperty("jdbc.ds" + count + ".id");
            // 如果获取id失败则退出循环
            if (StringUtils.isEmpty(id)) {
                break;
            }
            DataSource dbcpDs = this.initDataSource(props, count);
            // 设置默认数据库
            if ("true".equals(env.getProperty("jdbc.ds" + count + ".default"))) {
                ds.setDefaultTargetDataSource(dbcpDs); // ⑦
            }
            // 放入Map中
            targetDs.put(id, dbcpDs);
            // 保存id
            keyList.add(id);
            count++;
        } while (true);
        // 设置所有配置的数据源
        ds.setTargetDataSources(targetDs);
        return ds;
    }
}
