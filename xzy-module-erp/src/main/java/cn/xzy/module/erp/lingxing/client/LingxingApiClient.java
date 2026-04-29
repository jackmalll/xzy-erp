package cn.xzy.module.erp.lingxing.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.xzy.module.erp.lingxing.config.LingxingProperties;
import cn.xzy.module.erp.lingxing.sign.LingxingApiSign;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 领星 OpenAPI HTTP 客户端
 * 负责：1) 获取/续约 access_token；2) 发起签名业务 POST 请求
 */
@Slf4j
@Component
public class LingxingApiClient {

    @Resource
    private LingxingProperties properties;

    @Resource
    private LingxingTokenRedisDAO tokenRedisDAO;

    private static final String TOKEN_EXPIRED_CODE = "2001003";
    private static final String TOKEN_NOT_MATCH_CODE = "2001005";

    // ==================== Token 管理 ====================

    /**
     * 获取有效的 access_token
     * 优先级：Redis 缓存 → refresh_token 续约 → 重新获取
     */
    public String getValidAccessToken() {
        String accessToken = tokenRedisDAO.getAccessToken();
        if (accessToken != null) {
            return accessToken;
        }
        String refreshToken = tokenRedisDAO.getRefreshToken();
        if (refreshToken != null) {
            try {
                return refreshAccessToken(refreshToken);
            } catch (RuntimeException e) {
                log.warn("[LingxingApiClient][getValidAccessToken] refresh_token 续期失败，回退重新获取 token", e);
            }
        }
        return fetchNewAccessToken();
    }

    /**
     * 用 appId + appSecret 重新获取全新 token
     */
    private String fetchNewAccessToken() {
        String url = properties.getEndpoint() + "/api/auth-server/oauth/access-token";
        String body = applyProxy(HttpRequest.post(url))
                .form("appId", properties.getAppId())
                .form("appSecret", properties.getAppSecret())
                .timeout(30000)
                .execute()
                .body();
        return parseAndSaveToken(body, "fetchNewAccessToken");
    }

    /**
     * 用 refresh_token 续约 access_token（每个 refresh_token 只能使用一次）
     */
    private String refreshAccessToken(String refreshToken) {
        String url = properties.getEndpoint() + "/api/auth-server/oauth/refresh";
        String body = applyProxy(HttpRequest.post(url))
                .form("appId", properties.getAppId())
                .form("refreshToken", refreshToken)
                .timeout(30000)
                .execute()
                .body();
        return parseAndSaveToken(body, "refreshAccessToken");
    }

    private String parseAndSaveToken(String responseBody, String action) {
        JSONObject resp = JSONUtil.parseObj(responseBody);
        if (!"200".equals(resp.getStr("code"))) {
            tokenRedisDAO.deleteTokens();
            throw new RuntimeException("[LingxingApiClient][" + action + "] 失败：" + resp.getStr("msg"));
        }
        JSONObject data = resp.getJSONObject("data");
        String accessToken = data.getStr("access_token");
        String newRefreshToken = data.getStr("refresh_token");
        tokenRedisDAO.saveTokens(accessToken, newRefreshToken);
        log.info("[LingxingApiClient][{}] token 更新成功", action);
        return accessToken;
    }

    // ==================== 业务接口调用 ====================

    /**
     * 调用领星 POST 业务接口
     *
     * @param path       接口路径，例如 /erp/sc/routing/data/local_inventory/purchaseOrderList
     * @param bodyParams 业务请求参数（放入 body，json 格式）
     * @return 响应 JSON 字符串
     */
    public String post(String path, Map<String, Object> bodyParams) {
        String responseBody = doPost(path, bodyParams, getValidAccessToken());
        JSONObject resp = JSONUtil.parseObj(responseBody);
        if (isTokenInvalid(resp)) {
            log.warn("[LingxingApiClient][post] access_token 异常，path={}，code={}，尝试自动刷新后重试一次",
                    path, resp.getStr("code"));
            tokenRedisDAO.deleteAccessToken();
            responseBody = doPost(path, bodyParams, getValidAccessToken());
        }

        log.debug("[LingxingApiClient][post] path={}, response={}", path, responseBody);
        return responseBody;
    }

    private boolean isTokenInvalid(JSONObject resp) {
        String code = resp.getStr("code");
        return TOKEN_EXPIRED_CODE.equals(code) || TOKEN_NOT_MATCH_CODE.equals(code);
    }

    private String doPost(String path, Map<String, Object> bodyParams, String accessToken) {
        long timestamp = System.currentTimeMillis() / 1000;

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("access_token", accessToken);
        queryParams.put("app_key", properties.getAppId());
        queryParams.put("timestamp", String.valueOf(timestamp));

        // 签名：公共参数 + 业务参数合并后计算
        Map<String, Object> signMap = new HashMap<>(queryParams);
        signMap.putAll(bodyParams);
        String sign = LingxingApiSign.sign(signMap, properties.getAppId());

        try {
            queryParams.put("sign", URLEncoder.encode(sign, StandardCharsets.UTF_8.name()));
        } catch (Exception e) {
            throw new RuntimeException("sign url encode error", e);
        }

        StringBuilder queryStr = new StringBuilder();
        queryParams.forEach((k, v) -> queryStr.append(k).append("=").append(v).append("&"));
        String url = properties.getEndpoint() + path + "?" + queryStr;

        String responseBody = applyProxy(HttpRequest.post(url))
                .contentType("application/json")
                .body(JSONUtil.toJsonStr(bodyParams))
                .timeout(30000)
                .execute()
                .body();

        return responseBody;
    }

    private HttpRequest applyProxy(HttpRequest request) {
        if (StringUtils.hasText(properties.getProxyHost()) && properties.getProxyPort() > 0) {
            request.setHttpProxy(properties.getProxyHost(), properties.getProxyPort());
        }
        return request;
    }

}
