package cn.xzy.module.erp.lingxing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;

/**
 * 领星 OpenAPI 配置属性
 */
@ConfigurationProperties(prefix = "xzy.lingxing")
@Validated
@Data
public class LingxingProperties {

    /**
     * 领星 API 域名，例如 https://openapi.lingxing.com
     */
    @NotEmpty(message = "领星 endpoint 不能为空")
    private String endpoint;

    /**
     * 领星 AppId
     */
    @NotEmpty(message = "领星 appId 不能为空")
    private String appId;

    /**
     * 领星 AppSecret
     */
    @NotEmpty(message = "领星 appSecret 不能为空")
    private String appSecret;

    /**
     * 增量同步天数，每次同步拉取最近 N 天按 update_time 变化的数据
     * 默认 3 天
     */
    private int syncDays = 30;

}
