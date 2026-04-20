package cn.xzy.module.erp.lingxing.sign;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * 领星 AES/ECB/PKCS5Padding 加解密工具
 * 迁移自领星官方 SDK AesUtil.java
 */
public class LingxingAesUtil {

    private static final String ECB_MODE = "AES/ECB/PKCS5PADDING";

    private LingxingAesUtil() {
    }

    /**
     * AES ECB 加密
     *
     * @param password 待加密内容（MD5 大写字符串）
     * @param appKey   appId（16位，用作 AES 密钥）
     * @return Base64 编码的加密结果
     */
    public static String encryptEcb(String password, String appKey) {
        try {
            SecretKeySpec spec = new SecretKeySpec(appKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ECB_MODE);
            cipher.init(Cipher.ENCRYPT_MODE, spec);
            byte[] encrypted = cipher.doFinal(password.getBytes());
            return Base64.encodeBase64String(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("LingxingAesUtil encryptEcb error", e);
        }
    }

}
