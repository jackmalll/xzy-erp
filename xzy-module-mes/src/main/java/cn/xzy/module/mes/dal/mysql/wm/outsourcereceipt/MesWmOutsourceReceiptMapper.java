package cn.xzy.module.mes.dal.mysql.wm.outsourcereceipt;

import cn.xzy.framework.common.pojo.PageResult;
import cn.xzy.framework.mybatis.core.mapper.BaseMapperX;
import cn.xzy.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.xzy.module.mes.controller.admin.wm.outsourcereceipt.vo.MesWmOutsourceReceiptPageReqVO;
import cn.xzy.module.mes.dal.dataobject.wm.outsourcereceipt.MesWmOutsourceReceiptDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES 外协入库单 Mapper
 */
@Mapper
public interface MesWmOutsourceReceiptMapper extends BaseMapperX<MesWmOutsourceReceiptDO> {

    default PageResult<MesWmOutsourceReceiptDO> selectPage(MesWmOutsourceReceiptPageReqVO reqVO, java.util.Collection<Long> workOrderIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesWmOutsourceReceiptDO>()
                .likeIfPresent(MesWmOutsourceReceiptDO::getCode, reqVO.getCode())
                .likeIfPresent(MesWmOutsourceReceiptDO::getName, reqVO.getName())
                .eqIfPresent(MesWmOutsourceReceiptDO::getVendorId, reqVO.getVendorId())
                .eqIfPresent(MesWmOutsourceReceiptDO::getStatus, reqVO.getStatus())
                .inIfPresent(MesWmOutsourceReceiptDO::getWorkOrderId, workOrderIds)
                .betweenIfPresent(MesWmOutsourceReceiptDO::getReceiptDate, reqVO.getReceiptDate())
                .orderByDesc(MesWmOutsourceReceiptDO::getId));
    }

    default MesWmOutsourceReceiptDO selectByCode(String code) {
        return selectOne(MesWmOutsourceReceiptDO::getCode, code);
    }

}
