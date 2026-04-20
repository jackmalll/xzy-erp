package cn.xzy.module.erp.lingxing.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 领星集成配置类
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LingxingProperties.class)
public class LingxingConfiguration {

}
