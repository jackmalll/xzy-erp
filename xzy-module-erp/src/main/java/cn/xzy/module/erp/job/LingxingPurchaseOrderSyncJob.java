package cn.xzy.module.erp.job;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.xzy.framework.quartz.core.handler.JobHandler;
import cn.xzy.module.erp.service.purchase.ErpPurchaseOrderSyncService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 领星采购订单定时同步 Job
 *
 * 在芋道后台「基础设施 → 定时任务」中新增任务：
 *   处理器的名字：lingxingPurchaseOrderSyncJob
 *   CRON 表达式示例：0 0 * * * ?（每小时整点执行）
 *
 * 处理器的参数（可选，JSON 格式）：
 *   {"startDate":"2024-01-01 00:00:00","endDate":"2024-01-31 23:59:59"}
 *   不传参数则按配置的 syncDays 天数自动计算时间范围。
 */
@Slf4j
@Component
public class LingxingPurchaseOrderSyncJob implements JobHandler {

    @Resource
    private ErpPurchaseOrderSyncService erpPurchaseOrderSyncService;

    @Override
    public String execute(String param) throws Exception {
        String startDate = null;
        String endDate = null;

        if (StrUtil.isNotBlank(param)) {
            try {
                JSONObject paramObj = JSONUtil.parseObj(param);
                startDate = paramObj.getStr("startDate");
                endDate = paramObj.getStr("endDate");
            } catch (Exception e) {
                log.warn("[LingxingPurchaseOrderSyncJob][execute] 参数解析失败，将使用默认日期范围，param={}", param, e);
            }
        }

        log.info("[LingxingPurchaseOrderSyncJob][execute] 开始同步采购订单，startDate={}, endDate={}", startDate, endDate);
        String result = erpPurchaseOrderSyncService.syncPurchaseOrders(startDate, endDate);
        log.info("[LingxingPurchaseOrderSyncJob][execute] 同步完成：{}", result);
        return result;
    }

}
