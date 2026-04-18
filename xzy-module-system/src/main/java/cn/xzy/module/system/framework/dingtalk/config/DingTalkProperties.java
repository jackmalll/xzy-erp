package cn.xzy.module.system.framework.dingtalk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;

/**
 * 钉钉企业内应用配置项
 */
@ConfigurationProperties(prefix = "xzy.dingtalk")
@Data
@Validated
public class DingTalkProperties {

    /**
     * 钉钉企业内应用的 AppKey（即 client_id）
     */
    @NotEmpty(message = "钉钉 AppKey 不能为空")
    private String appKey;

    /**
     * 钉钉企业内应用的 AppSecret（即 client_secret）
     */
    @NotEmpty(message = "钉钉 AppSecret 不能为空")
    private String appSecret;

}
