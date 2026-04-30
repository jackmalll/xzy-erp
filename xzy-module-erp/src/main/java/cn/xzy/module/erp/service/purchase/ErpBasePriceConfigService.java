package cn.xzy.module.erp.service.purchase;

import cn.xzy.module.erp.controller.admin.purchase.vo.basepriceconfig.ErpBasePriceConfigVO;

/**
 * 基准单价（N）配置 Service 接口
 */
public interface ErpBasePriceConfigService {

    /**
     * 获取基准单价配置
     *
     * @return 配置 VO，若未配置则返回默认值
     */
    ErpBasePriceConfigVO getConfig();

    /**
     * 保存基准单价配置（新增或更新）
     *
     * @param configVO 配置 VO
     */
    void saveConfig(ErpBasePriceConfigVO configVO);

}
