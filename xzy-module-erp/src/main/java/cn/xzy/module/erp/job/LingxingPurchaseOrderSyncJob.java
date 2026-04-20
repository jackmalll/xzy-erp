package cn.xzy.module.erp.job;

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
 */
@Slf4j
@Component
public class LingxingPurchaseOrderSyncJob implements JobHandler {

    @Resource
    private ErpPurchaseOrderSyncService erpPurchaseOrderSyncService;

    @Override
    public String execute(String param) throws Exception {
        log.info("[LingxingPurchaseOrderSyncJob][execute] 开始同步采购订单");
        String result = erpPurchaseOrderSyncService.syncPurchaseOrders();
        log.info("[LingxingPurchaseOrderSyncJob][execute] 同步完成：{}", result);
        return result;
    }

}
