package cn.xzy.module.system.framework.dingtalk.core;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.xzy.module.system.framework.dingtalk.config.DingTalkProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static cn.xzy.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.xzy.module.system.enums.ErrorCodeConstants.AUTH_DINGTALK_AUTH_CODE_ERROR;

/**
 * 钉钉企业内应用 API 客户端
 */
@Component
@Slf4j
public class DingTalkClient {

    @Resource
    private DingTalkProperties dingTalkProperties;

    private static final String GET_TOKEN_URL = "https://oapi.dingtalk.com/gettoken";
    private static final String GET_USER_INFO_URL = "https://oapi.dingtalk.com/topapi/v2/user/getuserinfo";
    private static final String GET_USER_DETAIL_URL = "https://oapi.dingtalk.com/topapi/v2/user/get";
    private static final String LIST_SUB_DEPT_URL = "https://oapi.dingtalk.com/topapi/v2/department/listsub";

    /**
     * 获取钉钉 access_token
     *
     * @return access_token
     */
    private String getAccessToken() {
        String url = StrUtil.format("{}?appkey={}&appsecret={}",
                GET_TOKEN_URL, dingTalkProperties.getAppKey(), dingTalkProperties.getAppSecret());

        HttpResponse response = HttpRequest.get(url).execute();
        JSONObject result = JSONUtil.parseObj(response.body());

        if (result.getInt("errcode") != 0) {
            log.error("[getAccessToken][获取钉钉 access_token 失败：{}]", result);
            throw exception(AUTH_DINGTALK_AUTH_CODE_ERROR, result.getStr("errmsg"));
        }

        return result.getStr("access_token");
    }

    /**
     * 通过免登授权码获取用户信息
     *
     * @param authCode 免登授权码
     * @return 用户手机号
     */
    public String getUserMobileByAuthCode(String authCode) {
        // 1. 获取 access_token
        String accessToken = getAccessToken();

        // 2. 通过 authCode 获取 userId
        String userId = getUserIdByAuthCode(accessToken, authCode);

        // 3. 通过 userId 获取用户详细信息（包含手机号）
        return getUserMobileByUserId(accessToken, userId);
    }

    /**
     * 通过 authCode 获取 userId
     */
    private String getUserIdByAuthCode(String accessToken, String authCode) {
        String url = StrUtil.format("{}?access_token={}", GET_USER_INFO_URL, accessToken);

        JSONObject body = new JSONObject();
        body.set("code", authCode);

        HttpResponse response = HttpRequest.post(url)
                .body(body.toString())
                .execute();

        JSONObject result = JSONUtil.parseObj(response.body());

        if (result.getInt("errcode") != 0) {
            log.error("[getUserIdByAuthCode][通过 authCode 获取 userId 失败：authCode={}, result={}]", authCode, result);
            throw exception(AUTH_DINGTALK_AUTH_CODE_ERROR, result.getStr("errmsg"));
        }

        return result.getJSONObject("result").getStr("userid");
    }

    /**
     * 通过 userId 获取用户手机号
     */
    private String getUserMobileByUserId(String accessToken, String userId) {
        String url = StrUtil.format("{}?access_token={}", GET_USER_DETAIL_URL, accessToken);

        JSONObject body = new JSONObject();
        body.set("userid", userId);

        HttpResponse response = HttpRequest.post(url)
                .body(body.toString())
                .execute();

        JSONObject result = JSONUtil.parseObj(response.body());

        if (result.getInt("errcode") != 0) {
            log.error("[getUserMobileByUserId][通过 userId 获取手机号失败：userId={}, result={}]", userId, result);
            throw exception(AUTH_DINGTALK_AUTH_CODE_ERROR, result.getStr("errmsg"));
        }

        return result.getJSONObject("result").getStr("mobile");
    }

    /**
     * 获取钉钉部门列表（递归获取指定父部门下所有子部门）
     *
     * @param parentId 父部门ID，根部门传 1
     * @return 部门信息列表，每个元素包含 dept_id、name、parent_id、order 字段
     */
    public List<JSONObject> getDeptList(Long parentId) {
        String accessToken = getAccessToken();
        List<JSONObject> allDepts = new ArrayList<>();
        fetchDeptListRecursive(accessToken, parentId, allDepts);
        return allDepts;
    }

    /**
     * 递归拉取子部门列表
     */
    private void fetchDeptListRecursive(String accessToken, Long parentId, List<JSONObject> result) {
        String url = StrUtil.format("{}?access_token={}", LIST_SUB_DEPT_URL, accessToken);

        JSONObject body = new JSONObject();
        body.set("dept_id", parentId);

        HttpResponse response = HttpRequest.post(url)
                .body(body.toString())
                .execute();

        JSONObject res = JSONUtil.parseObj(response.body());

        if (res.getInt("errcode") != 0) {
            log.error("[getDeptList][获取钉钉部门列表失败：parentId={}, result={}]", parentId, res);
            throw exception(AUTH_DINGTALK_AUTH_CODE_ERROR, res.getStr("errmsg"));
        }

        List<JSONObject> depts = JSONUtil.toList(res.getJSONArray("result"), JSONObject.class);
        if (depts == null || depts.isEmpty()) {
            return;
        }
        result.addAll(depts);
        for (JSONObject dept : depts) {
            fetchDeptListRecursive(accessToken, dept.getLong("dept_id"), result);
        }
    }

}
