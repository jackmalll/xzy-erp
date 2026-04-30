package cn.xzy.module.erp.controller.admin.purchase;

import cn.xzy.framework.common.pojo.CommonResult;
import cn.xzy.module.erp.controller.admin.purchase.vo.basepriceconfig.ErpBasePriceConfigVO;
import cn.xzy.module.erp.service.purchase.ErpBasePriceConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.xzy.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 基准单价（N）配置")
@RestController
@RequestMapping("/erp/base-price-config")
@Validated
public class ErpBasePriceConfigController {

    @Resource
    private ErpBasePriceConfigService basePriceConfigService;

    @GetMapping("/get")
    @Operation(summary = "获取基准单价配置")
    @PreAuthorize("@ss.hasPermission('erp:base-price-config:query')")
    public CommonResult<ErpBasePriceConfigVO> getConfig() {
        return success(basePriceConfigService.getConfig());
    }

    @PostMapping("/save")
    @Operation(summary = "保存基准单价配置")
    @PreAuthorize("@ss.hasPermission('erp:base-price-config:save')")
    public CommonResult<Boolean> saveConfig(@Valid @RequestBody ErpBasePriceConfigVO configVO) {
        basePriceConfigService.saveConfig(configVO);
        return success(true);
    }

}
