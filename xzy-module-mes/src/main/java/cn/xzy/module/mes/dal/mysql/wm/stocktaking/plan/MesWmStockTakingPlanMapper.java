package cn.xzy.module.mes.dal.mysql.wm.stocktaking.plan;

import cn.xzy.framework.common.pojo.PageResult;
import cn.xzy.framework.mybatis.core.mapper.BaseMapperX;
import cn.xzy.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.xzy.module.mes.controller.admin.wm.stocktaking.plan.vo.MesWmStockTakingPlanPageReqVO;
import cn.xzy.module.mes.dal.dataobject.wm.stocktaking.plan.MesWmStockTakingPlanDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 盘点方案 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesWmStockTakingPlanMapper extends BaseMapperX<MesWmStockTakingPlanDO> {

    default MesWmStockTakingPlanDO selectByCode(String code) {
        return selectOne(MesWmStockTakingPlanDO::getCode, code);
    }

    default PageResult<MesWmStockTakingPlanDO> selectPage(MesWmStockTakingPlanPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesWmStockTakingPlanDO>()
                .likeIfPresent(MesWmStockTakingPlanDO::getCode, reqVO.getCode())
                .likeIfPresent(MesWmStockTakingPlanDO::getName, reqVO.getName())
                .eqIfPresent(MesWmStockTakingPlanDO::getType, reqVO.getType())
                .orderByDesc(MesWmStockTakingPlanDO::getId));
    }

    default List<MesWmStockTakingPlanDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<MesWmStockTakingPlanDO>()
                .eqIfPresent(MesWmStockTakingPlanDO::getStatus, status)
                .orderByDesc(MesWmStockTakingPlanDO::getId));
    }

}
