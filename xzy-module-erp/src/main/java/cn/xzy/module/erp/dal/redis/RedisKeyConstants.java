package cn.xzy.module.erp.dal.redis;

/**
 * ERP Redis Key 枚举类
 *
 * @author 芋道源码
 */
public interface RedisKeyConstants {

    /**
     * 序号的缓存
     *
     * KEY 格式：trade_no:{prefix}
     * VALUE 数据格式：编号自增
     */
    String NO = "erp:seq_no:";

    /**
     * 领星 access_token 缓存
     * KEY 固定值；VALUE：access_token 字符串；TTL = 7000s
     */
    String LINGXING_ACCESS_TOKEN = "erp:lingxing:access_token";

    /**
     * 领星 refresh_token 缓存
     * KEY 固定值；VALUE：refresh_token 字符串；TTL = 7000s
     */
    String LINGXING_REFRESH_TOKEN = "erp:lingxing:refresh_token";

}
