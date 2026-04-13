package cn.xzy.module.iot.dal.mysql.ota;

import cn.xzy.framework.common.pojo.PageResult;
import cn.xzy.framework.mybatis.core.mapper.BaseMapperX;
import cn.xzy.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.xzy.module.iot.controller.admin.ota.vo.firmware.IotOtaFirmwarePageReqVO;
import cn.xzy.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IotOtaFirmwareMapper extends BaseMapperX<IotOtaFirmwareDO> {

    default IotOtaFirmwareDO selectByProductIdAndVersion(Long productId, String version) {
        return selectOne(IotOtaFirmwareDO::getProductId, productId,
                IotOtaFirmwareDO::getVersion, version);
    }

    default PageResult<IotOtaFirmwareDO> selectPage(IotOtaFirmwarePageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<IotOtaFirmwareDO>()
                .likeIfPresent(IotOtaFirmwareDO::getName, pageReqVO.getName())
                .eqIfPresent(IotOtaFirmwareDO::getProductId, pageReqVO.getProductId())
                .betweenIfPresent(IotOtaFirmwareDO::getCreateTime, pageReqVO.getCreateTime())
                .orderByDesc(IotOtaFirmwareDO::getCreateTime));
    }

}
