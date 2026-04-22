package cn.xzy.module.system.job;

import cn.hutool.json.JSONObject;
import cn.xzy.framework.common.enums.CommonStatusEnum;
import cn.xzy.framework.quartz.core.handler.JobHandler;
import cn.xzy.module.system.dal.dataobject.dept.DeptDO;
import cn.xzy.module.system.dal.mysql.dept.DeptMapper;
import cn.xzy.module.system.framework.dingtalk.core.DingTalkClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 同步钉钉部门列表 Job
 *
 * <p>调用钉钉 topapi/v2/department/listsub 接口，递归获取所有部门，
 * 并将钉钉部门数据同步到本系统 system_dept 表中。</p>
 *
 * <p>父部门ID映射规则：
 * <ul>
 *   <li>钉钉根部门（dept_id=1）的子部门，parent_id 映射为 {@link #SYSTEM_ROOT_PARENT_ID}（100）</li>
 *   <li>其他部门的 parent_id 使用入库后实际生成的数据库 ID</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
public class SyncDingTalkDeptJob implements JobHandler {

    /**
     * 钉钉根部门 ID，固定为 1
     */
    private static final Long DING_TALK_ROOT_DEPT_ID = 1L;

    /**
     * 钉钉根部门在系统中对应的父部门 ID
     */
    private static final Long SYSTEM_ROOT_PARENT_ID = 100L;

    @Resource
    private DingTalkClient dingTalkClient;

    @Resource
    private DeptMapper deptMapper;

    @Override
    public String execute(String param) {
        log.info("[execute][开始同步钉钉部门列表]");

        // 1. 从钉钉拉取所有部门（DingTalkClient 内部按层级递归，父部门先于子部门返回）
        List<JSONObject> dingDepts = dingTalkClient.getDeptList(DING_TALK_ROOT_DEPT_ID);
        log.info("[execute][从钉钉获取到 {} 个部门]", dingDepts.size());

        // 2. 维护「钉钉 deptId → 系统数据库 deptId」映射表
        //    钉钉根部门 1 → 系统父节点 SYSTEM_ROOT_PARENT_ID
        Map<Long, Long> dingIdToSystemId = new HashMap<>();
        dingIdToSystemId.put(DING_TALK_ROOT_DEPT_ID, SYSTEM_ROOT_PARENT_ID);

        int createCount = 0;
        int updateCount = 0;

        // 3. 按顺序处理（getDeptList 递归返回保证父部门先于子部门）
        for (JSONObject dingDept : dingDepts) {
            Long dingDeptId = dingDept.getLong("dept_id");
            String name = dingDept.getStr("name");
            Long dingParentId = dingDept.getLong("parent_id");
            Integer order = dingDept.getInt("order");

            // 3.1 将钉钉 parent_id 转换为系统数据库 parent_id
            Long systemParentId = dingIdToSystemId.getOrDefault(dingParentId, SYSTEM_ROOT_PARENT_ID);

            // 3.2 按系统 parentId + name 查重
            DeptDO existDept = deptMapper.selectByParentIdAndName(systemParentId, name);

            if (existDept == null) {
                // 3.3 不存在则新增，insert 后 MyBatis-Plus 回填主键
                DeptDO dept = new DeptDO();
                dept.setName(name);
                dept.setParentId(systemParentId);
                dept.setSort(order != null ? order : 0);
                dept.setStatus(CommonStatusEnum.ENABLE.getStatus());
                deptMapper.insert(dept);

                // 记录钉钉ID → 新入库系统ID的映射
                dingIdToSystemId.put(dingDeptId, dept.getId());
                createCount++;
                log.debug("[execute][新增部门：dingDeptId={}, systemId={}, name={}]", dingDeptId, dept.getId(), name);
            } else {
                // 3.4 已存在则更新排序和状态，并记录映射
                DeptDO updateDept = new DeptDO();
                updateDept.setId(existDept.getId());
                updateDept.setSort(order != null ? order : existDept.getSort());
                updateDept.setStatus(CommonStatusEnum.ENABLE.getStatus());
                deptMapper.updateById(updateDept);

                dingIdToSystemId.put(dingDeptId, existDept.getId());
                updateCount++;
                log.debug("[execute][更新部门：dingDeptId={}, systemId={}, name={}]", dingDeptId, existDept.getId(), name);
            }
        }

        String result = String.format("同步钉钉部门完成：共 %d 个部门，新增 %d 个，更新 %d 个",
                dingDepts.size(), createCount, updateCount);
        log.info("[execute][{}]", result);
        return result;
    }

}
