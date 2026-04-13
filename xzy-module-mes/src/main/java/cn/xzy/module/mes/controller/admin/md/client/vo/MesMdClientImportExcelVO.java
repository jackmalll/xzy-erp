package cn.xzy.module.mes.controller.admin.md.client.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.xzy.framework.excel.core.annotations.DictFormat;
import cn.xzy.framework.excel.core.convert.DictConvert;
import cn.xzy.module.mes.enums.DictTypeConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 客户 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MesMdClientImportExcelVO {

    @ExcelProperty("客户编码")
    private String code;

    @ExcelProperty("客户名称")
    private String name;

    @ExcelProperty("客户简称")
    private String nickname;

    @ExcelProperty(value = "客户类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_CLIENT_TYPE)
    private Integer type;

    @ExcelProperty("客户电话")
    private String telephone;

    @ExcelProperty("客户邮箱地址")
    private String email;

    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(cn.xzy.module.system.enums.DictTypeConstants.COMMON_STATUS)
    private Integer status;

}
