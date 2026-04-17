package cn.xzy.module.system.service.permission;

import cn.xzy.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;

import java.util.Collection;
import java.util.Set;

import static java.util.Collections.singleton;

/**
 * 权限 Service 接口
 * <p>
 * 提供用户-角色、角色-菜单、角色-部门的关联权限处理
 *
 * @author 芋道源码
 */
public interface PermissionService {

    /**
     * 判断是否有权限，任一一个即可
     *
     * @param userId      用户编号
     * @param permissions 权限
     * @return 是否
     */
    boolean hasAnyPermissions(Long userId, String... permissions);

    /**
     * 判断是否有角色，任一一个即可
     *
     * @param roles 角色数组
     * @return 是否
     */
    boolean hasAnyRoles(Long userId, String... roles);

    // ========== 角色-菜单的相关方法  ==========

    /**
     * 设置角色菜单（带操作者越权校验）
     * 非超级管理员只能给角色分配自己拥有的菜单权限子集
     *
     * @param operatorUserId 操作者用户编号
     * @param roleId         角色编号
     * @param menuIds        菜单编号集合
     */
    void assignRoleMenu(Long operatorUserId, Long roleId, Set<Long> menuIds);

    /**
     * 设置角色菜单（无越权校验，内部/超管专用）
     *
     * @param roleId  角色编号
     * @param menuIds 菜单编号集合
     */
    void assignRoleMenu(Long roleId, Set<Long> menuIds);

    /**
     * 获得操作者可以分配给角色的菜单编号集合（即操作者自身拥有的菜单，超管返回全量）
     *
     * @param operatorUserId 操作者用户编号
     * @return 可分配菜单编号集合
     */
    Set<Long> getAssignableMenuIdsByOperator(Long operatorUserId);

    /**
     * 获得操作者的部门管辖范围（本部门 + 所有子部门的 ID 集合）
     * 超级管理员返回 null，表示不做部门限制
     *
     * @param operatorUserId 操作者用户编号
     * @return 部门编号集合，null 表示无限制
     */
    Set<Long> getOperatorDeptScope(Long operatorUserId);

    /**
     * 处理角色删除时，删除关联授权数据
     *
     * @param roleId 角色编号
     */
    void processRoleDeleted(Long roleId);

    /**
     * 处理菜单删除时，删除关联授权数据
     *
     * @param menuId 菜单编号
     */
    void processMenuDeleted(Long menuId);

    /**
     * 获得角色拥有的菜单编号集合
     *
     * @param roleId 角色编号
     * @return 菜单编号集合
     */
    default Set<Long> getRoleMenuListByRoleId(Long roleId) {
        return getRoleMenuListByRoleId(singleton(roleId));
    }

    /**
     * 获得角色们拥有的菜单编号集合
     *
     * @param roleIds 角色编号数组
     * @return 菜单编号集合
     */
    Set<Long> getRoleMenuListByRoleId(Collection<Long> roleIds);

    /**
     * 获得拥有指定菜单的角色编号数组，从缓存中获取
     *
     * @param menuId 菜单编号
     * @return 角色编号数组
     */
    Set<Long> getMenuRoleIdListByMenuIdFromCache(Long menuId);

    // ========== 用户-角色的相关方法  ==========

    /**
     * 设置用户角色（带操作者越权校验）
     * 非超级管理员只能给用户分配自己拥有的角色子集
     *
     * @param operatorUserId 操作者用户编号
     * @param userId         被操作用户编号
     * @param roleIds        角色编号集合
     */
    void assignUserRole(Long operatorUserId, Long userId, Set<Long> roleIds);

    /**
     * 设置用户角色（无越权校验，内部/超管专用）
     *
     * @param userId  用户编号
     * @param roleIds 角色编号集合
     */
    void assignUserRole(Long userId, Set<Long> roleIds);

    /**
     * 处理用户删除时，删除关联授权数据
     *
     * @param userId 用户编号
     */
    void processUserDeleted(Long userId);

    /**
     * 获得拥有多个角色的用户编号集合
     *
     * @param roleIds 角色编号集合
     * @return 用户编号集合
     */
    Set<Long> getUserRoleIdListByRoleId(Collection<Long> roleIds);

    /**
     * 获得用户拥有的角色编号集合
     *
     * @param userId 用户编号
     * @return 角色编号集合
     */
    Set<Long> getUserRoleIdListByUserId(Long userId);

    /**
     * 获得用户拥有的角色编号集合，从缓存中获取
     *
     * @param userId 用户编号
     * @return 角色编号集合
     */
    Set<Long> getUserRoleIdListByUserIdFromCache(Long userId);

    // ========== 用户-部门的相关方法  ==========

    /**
     * 设置角色的数据权限
     *
     * @param roleId           角色编号
     * @param dataScope        数据范围
     * @param dataScopeDeptIds 部门编号数组
     */
    void assignRoleDataScope(Long roleId, Integer dataScope, Set<Long> dataScopeDeptIds);

    /**
     * 获得登陆用户的部门数据权限
     *
     * @param userId 用户编号
     * @return 部门数据权限
     */
    DeptDataPermissionRespDTO getDeptDataPermission(Long userId);

    /**
     * 获得操作者可分配给用户的角色编号集合
     * 超管：返回全量角色（含超管角色）
     * 非超管：返回本部门+子部门所有用户持有的角色集合（排除超管角色），与 assignUserRole 越权校验标准一致
     *
     * @param operatorUserId 操作者用户编号
     * @return 可分配角色编号集合
     */
    Set<Long> getAssignableRoleIdsByOperator(Long operatorUserId);

}
