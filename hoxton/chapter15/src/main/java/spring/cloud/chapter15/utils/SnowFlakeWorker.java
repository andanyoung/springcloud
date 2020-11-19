package spring.cloud.chapter15.utils;

public class SnowFlakeWorker {

    // 开始时间（这里使用2019年4月1日整点）
    private final static long START_TIME = 1554048000000L;
    // 数据中心编号所占位数
    private final static long DATA_CENTER_BITS = 10L;
    // 最大数据中心编号
    private final static long MAX_DATA_CENTER_ID = 1023;
    // 序列编号占位位数
    private final static long SEQUENCE_BIT = 12L;
    // 数据中心编号向左移12位
    private final static long DATA_CENTER_SHIFT = SEQUENCE_BIT;
    /**
     * 时间戳向左移22位(10+12)
     */
    private final static long TIMESTAMP_SHIFT
            = DATA_CENTER_BITS + DATA_CENTER_SHIFT;
    // 最大生成序列号，这里为4095
    private final static long MAX_SEQUENCE = 4095;
    // 数据中心ID(0~1023)
    private long dataCenterId;
    // 毫秒内序列（0~4095）
    private long sequence = 0L;
    // 上次生成ID的时间戳
    private long lastTimestamp = -1L;

    /**
     * 因为当前微服务和分布式趋向于去中心化，所以不存在受理机器编号，
     * 10位二进制全部用于数据中心
     *
     * @param dataCenterId -- 数据中心ID [0~1023]
     */
    public SnowFlakeWorker(long dataCenterId) { // ①   // 验证数据中心编号的合法性
        if (dataCenterId > MAX_DATA_CENTER_ID) {
            String msg = "数据中心编号[" + dataCenterId
                    + "]超过最大允许值【" + MAX_DATA_CENTER_ID + "】";
            throw new RuntimeException(msg);
        }
        if (dataCenterId < 0) {
            String msg = "数据中心编号[" + dataCenterId + "]不允许小于0";
            throw new RuntimeException(msg);
        }
        this.dataCenterId = dataCenterId;
    }

    /**
     * 获得下一个ID (为了避免多线程环境产生的错误，这里方法是线程安全的)
     *
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        // 获取当前时间
        long timestamp = System.currentTimeMillis();
        // 如果是同一个毫秒时间戳的处理
        if (timestamp == lastTimestamp) {
            sequence += 1; // 序号+1
            // 是否超过允许的最大序列
            if (sequence > MAX_SEQUENCE) {
                sequence = 0;
                // 等待到下一毫秒
                timestamp = tilNextMillis(timestamp); // ②
            }
        } else {
            // 修改时间戳
            lastTimestamp = timestamp;
            // 序号重新开始
            sequence = 0;
        }
        // 二进制的位运算，其中“<<”代表二进制左移，“|”代表或运算
        return ((timestamp - START_TIME) << TIMESTAMP_SHIFT) | (this.dataCenterId << DATA_CENTER_SHIFT) | sequence; //③
    }

    /**
     * 阻塞到下一毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp -- 上次生成ID的时间戳
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp;
        do {
            timestamp = System.currentTimeMillis();
        } while (timestamp > lastTimestamp);
        return timestamp;
    }
}
