package cn.xzy.module.erp.lingxing.client;

import cn.xzy.module.erp.dal.redis.RedisKeyConstants;
import cn.xzy.module.erp.lingxing.config.LingxingProperties;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

/**
 * 领星 Token Redis DAO
 * access_token / refresh_token 均只存 Redis，TTL=7000s（expires_in=7199s，留 199s 余量）
 */
@Repository
public class LingxingTokenRedisDAO {

    private static final Duration TOKEN_TTL = Duration.ofSeconds(7000);

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private LingxingProperties lingxingProperties;

    public String getAccessToken() {
        return stringRedisTemplate.opsForValue().get(buildAccessTokenKey());
    }

    public String getRefreshToken() {
        return stringRedisTemplate.opsForValue().get(buildRefreshTokenKey());
    }

    public void saveTokens(String accessToken, String refreshToken) {
        stringRedisTemplate.opsForValue().set(buildAccessTokenKey(), accessToken, TOKEN_TTL);
        stringRedisTemplate.opsForValue().set(buildRefreshTokenKey(), refreshToken, TOKEN_TTL);
    }

    public void deleteAccessToken() {
        stringRedisTemplate.delete(buildAccessTokenKey());
    }

    public void deleteTokens() {
        stringRedisTemplate.delete(buildAccessTokenKey());
        stringRedisTemplate.delete(buildRefreshTokenKey());
    }

    private String buildAccessTokenKey() {
        return RedisKeyConstants.LINGXING_ACCESS_TOKEN + ":" + lingxingProperties.getAppId();
    }

    private String buildRefreshTokenKey() {
        return RedisKeyConstants.LINGXING_REFRESH_TOKEN + ":" + lingxingProperties.getAppId();
    }

}
