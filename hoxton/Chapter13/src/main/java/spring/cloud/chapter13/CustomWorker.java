package spring.cloud.chapter13;

public class CustomWorker {
    // 开始时间（这里使用2019年4月1日整点）
    private final static long START_TIME = 1554048000000L;
    // 当前发号节点编号（最大值63）
    private static long MACHINE_ID = 21L;
    // 最大数据中心编号
    private final static long MAX_DATA_CENTER_ID = 127L;
    // 最大序列号
    private final static long MAX_SEQUENCE = 255L;
    // 数据中心位数
    private final static long DATA_CENTER_BIT = 8L;
    // 机器中心位数
    private final static long MACHINE_BIT = 6L;
    // 序列编号占位位数
    private final static long SEQUENCE_BIT = 8L;
    // 数据中心移位（8位）
    private final static long DATA_CENTER_SHIFT = SEQUENCE_BIT;
    // 当前发号节点移位（8+8=16位）
    private final static long MACHINE_SHIFT
            = SEQUENCE_BIT + DATA_CENTER_BIT;
    // 时间戳移位（8+8+6=22位）
    private final static long TIMESTAMP_SHIFT
            = SEQUENCE_BIT + DATA_CENTER_BIT + MACHINE_BIT;

    // 数据中心编号
    private long dataCenterId;
    // 序号
    private long sequence = 0;
    // 上次时间戳
    private long lastTimestamp;


    public CustomWorker(long dataCenterId) {
        // 验证数据中心编号的合法性
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
     * 获得下一个ID （该方法是线程安全的）
     *
     * @return 下一个ID
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
                timestamp = tilNextMillis(timestamp);
            }
        } else {
            // 修改时间戳
            lastTimestamp = timestamp;
            // 序号重新开始
            sequence = 0;
        }
        // 二进制的位运算，其中“<<”代表二进制左移，“|”代表或运算
        long result = ((timestamp - START_TIME) << TIMESTAMP_SHIFT)
                | (MACHINE_ID << MACHINE_SHIFT)
                | (this.dataCenterId << DATA_CENTER_SHIFT)
                | sequence;
        return result;
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
