package spring.cloud.chapter14.datasource;

public class DataSourcesContentHolder {
    // 线程副本
    private static final ThreadLocal<Long> contextHolder = new ThreadLocal<>();

    // 设置id
    public static void setId(Long id) {
        contextHolder.set(id);
    }

    // 获取线程id
    public static Long getId() {
        return contextHolder.get();
    }
}
