package cn.xzy.module.erp.service.purchase;

import cn.xzy.module.erp.controller.admin.purchase.vo.basepriceconfig.ErpBasePriceConfigVO;
import cn.xzy.module.infra.controller.admin.config.vo.ConfigSaveReqVO;
import cn.xzy.module.infra.dal.dataobject.config.ConfigDO;
import cn.xzy.module.infra.service.config.ConfigService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 基准单价（N）配置 Service 实现
 */
@Service
@Slf4j
public class ErpBasePriceConfigServiceImpl implements ErpBasePriceConfigService {

    /** infra_config 表中存储本配置的 key */
    private static final String CONFIG_KEY = "erp.basePriceConfig";

    @Resource
    private ConfigService configService;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public ErpBasePriceConfigVO getConfig() {
        ConfigDO configDO = configService.getConfigByKey(CONFIG_KEY);
        if (configDO == null || configDO.getValue() == null) {
            return buildDefault();
        }
        try {
            return objectMapper.readValue(configDO.getValue(), ErpBasePriceConfigVO.class);
        } catch (JsonProcessingException e) {
            log.warn("基准单价配置反序列化失败，返回默认配置", e);
            return buildDefault();
        }
    }

    @Override
    public void saveConfig(ErpBasePriceConfigVO configVO) {
        String json;
        try {
            json = objectMapper.writeValueAsString(configVO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("基准单价配置序列化失败", e);
        }

        ConfigDO existing = configService.getConfigByKey(CONFIG_KEY);
        ConfigSaveReqVO saveReqVO = new ConfigSaveReqVO();
        saveReqVO.setCategory("erp");
        saveReqVO.setName("基准单价（N）配置");
        saveReqVO.setKey(CONFIG_KEY);
        saveReqVO.setValue(json);
        saveReqVO.setVisible(false);
        saveReqVO.setRemark("ERP采购基准单价计算规则及风控配置");
        if (existing == null) {
            configService.createConfig(saveReqVO);
        } else {
            saveReqVO.setId(existing.getId());
            configService.updateConfig(saveReqVO);
        }
    }

    private ErpBasePriceConfigVO buildDefault() {
        ErpBasePriceConfigVO vo = new ErpBasePriceConfigVO();
        vo.setRuleMinPrice(false);
        vo.setRuleThreeEnabled(false);
        vo.setRule1MaxCount(3);
        vo.setRule1AvgFrom(1);
        vo.setRule1AvgTo(3);
        vo.setRule2MinCount(3);
        vo.setRule2MaxCount(10);
        vo.setRule2AvgFrom(5);
        vo.setRule2AvgTo(8);
        vo.setRule3MinCount(10);
        vo.setRule3AvgFrom(5);
        vo.setRule3AvgTo(8);
        vo.setRiskQtyEnabled(false);
        vo.setRiskQtyThreshold(null);
        vo.setRiskAmountEnabled(false);
        vo.setRiskAmountThreshold(null);
        vo.setRiskPriceFloatEnabled(false);
        vo.setRiskPriceFloatPercent(null);
        return vo;
    }

}
