package cn.xzy.module.erp.lingxing.sign;

import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

/**
 * 领星 API 签名生成工具
 * 规则：参数 ASCII 排序 → key=value& 拼接（空字符串跳过）→ MD5(32位)大写 → AES/ECB 加密
 * 迁移自领星官方 SDK ApiSign.java
 */
public class LingxingApiSign {

    private LingxingApiSign() {
    }

    /**
     * 生成签名
     *
     * @param params    所有参与签名的参数（公共参数 + 业务参数）；value 为空字符串则跳过，null 会被转为 "null" 参与签名
     * @param appId     appId（同时用作 AES 加密密钥）
     * @return 签名字符串（调用方需在拼 URL 前做 URLEncoder.encode）
     */
    public static String sign(Map<String, Object> params, String appId) {
        String[] keys = params.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            String value = String.valueOf(params.get(key));
            if (value.isEmpty()) {
                continue;
            }
            sb.append(key).append("=").append(value.trim()).append("&");
        }
        String paramStr = sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
        String md5Hex = DigestUtils.md5Hex(paramStr.getBytes(StandardCharsets.UTF_8)).toUpperCase();
        return LingxingAesUtil.encryptEcb(md5Hex, appId);
    }

}
