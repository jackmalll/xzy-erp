package cn.xzy.module.system.controller.admin.permission;

import cn.hutool.core.collection.CollUtil;
import cn.xzy.framework.common.pojo.CommonResult;
import cn.xzy.framework.datapermission.core.annotation.DataPermission;
import cn.xzy.framework.security.core.util.SecurityFrameworkUtils;
import cn.xzy.module.system.controller.admin.permission.vo.permission.PermissionAssignRoleDataScopeReqVO;
import cn.xzy.module.system.controller.admin.permission.vo.permission.PermissionAssignRoleMenuReqVO;
import cn.xzy.module.system.controller.admin.permission.vo.permission.PermissionAssignUserRoleReqVO;
import cn.xzy.module.system.service.permission.PermissionService;
import cn.xzy.module.system.service.permission.RoleService;
import cn.xzy.module.system.service.tenant.TenantService;
import cn.xzy.module.system.service.user.AdminUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Set;

import static cn.xzy.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.xzy.framework.common.pojo.CommonResult.success;
import static cn.xzy.module.system.enums.ErrorCodeConstants.PERMISSION_USER_NOT_IN_OPERATOR_DEPT;

/**
 * 权限 Controller，提供赋予用户、角色的权限的 API 接口
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - 权限")
@RestController
@RequestMapping("/system/permission")
public class PermissionController {

    @Resource
    private PermissionService permissionService;
    @Resource
    private RoleService roleService;
    @Resource
    private TenantService tenantService;
    @Resource
    private AdminUserService userService;

    @Operation(summary = "获得角色拥有的菜单编号")
    @Parameter(name = "roleId", description = "角色编号", required = true)
    @GetMapping("/list-role-menus")
    @PreAuthorize("@ss.hasPermission('system:permission:assign-role-menu')")
    public CommonResult<Set<Long>> getRoleMenuList(Long roleId) {
        Set<Long> roleMenuIds = permissionService.getRoleMenuListByRoleId(roleId);
        // 非超管：返回的已选中菜单必须在操作者可分配范围内，防止前端回显超周定选中项
        Long operatorUserId = SecurityFrameworkUtils.getLoginUserId();
        Set<Long> operatorRoleIds = permissionService.getUserRoleIdListByUserId(operatorUserId);
        if (!roleService.hasAnySuperAdmin(operatorRoleIds)) {
            Set<Long> assignableMenuIds = permissionService.getAssignableMenuIdsByOperator(operatorUserId);
            roleMenuIds = CollUtil.intersectionDistinct(roleMenuIds, assignableMenuIds);
        }
        return success(roleMenuIds);
    }

    @PostMapping("/assign-role-menu")
    @Operation(summary = "赋予角色菜单")
    @PreAuthorize("@ss.hasPermission('system:permission:assign-role-menu')")
    public CommonResult<Boolean> assignRoleMenu(@Validated @RequestBody PermissionAssignRoleMenuReqVO reqVO) {
        // 开启多租户的情况下，需要过滤掉未开通的菜单
        tenantService.handleTenantMenu(menuIds -> reqVO.getMenuIds().removeIf(menuId -> !CollUtil.contains(menuIds, menuId)));

        // 执行菜单的分配（带操作者越权校验）
        Long operatorUserId = SecurityFrameworkUtils.getLoginUserId();
        permissionService.assignRoleMenu(operatorUserId, reqVO.getRoleId(), reqVO.getMenuIds());
        return success(true);
    }

    @PostMapping("/assign-role-data-scope")
    @Operation(summary = "赋予角色数据权限")
    @PreAuthorize("@ss.hasPermission('system:permission:assign-role-data-scope')")
    public CommonResult<Boolean> assignRoleDataScope(@Valid @RequestBody PermissionAssignRoleDataScopeReqVO reqVO) {
        permissionService.assignRoleDataScope(reqVO.getRoleId(), reqVO.getDataScope(), reqVO.getDataScopeDeptIds());
        return success(true);
    }

    @Operation(summary = "获得用户拥有的角色编号列表")
    @Parameter(name = "userId", description = "用户编号", required = true)
    @GetMapping("/list-user-roles")
    @PreAuthorize("@ss.hasPermission('system:permission:assign-user-role')")
    @DataPermission(enable = false)
    public CommonResult<Set<Long>> listAdminRoles(@RequestParam("userId") Long userId) {
        Long operatorUserId = SecurityFrameworkUtils.getLoginUserId();
        Set<Long> operatorRoleIds = permissionService.getUserRoleIdListByUserId(operatorUserId);
        if (!roleService.hasAnySuperAdmin(operatorRoleIds)) {
            // 非超管：校验目标用户必须在操作者的部门管辖范围内
            Set<Long> deptScope = permissionService.getOperatorDeptScope(operatorUserId);
            if (CollUtil.isNotEmpty(deptScope)) {
                var targetUser = userService.getUser(userId);
                if (targetUser == null || !deptScope.contains(targetUser.getDeptId())) {
                    throw exception(PERMISSION_USER_NOT_IN_OPERATOR_DEPT);
                }
            }
            // 已选中角色限制在部门范围内可分配的角色范围内
            Set<Long> userRoleIds = permissionService.getUserRoleIdListByUserId(userId);
            Set<Long> assignableRoleIds = permissionService.getAssignableRoleIdsByOperator(operatorUserId);
            return success(CollUtil.intersectionDistinct(userRoleIds, assignableRoleIds));
        }
        return success(permissionService.getUserRoleIdListByUserId(userId));
    }

    @Operation(summary = "赋予用户角色")
    @PostMapping("/assign-user-role")
    @PreAuthorize("@ss.hasPermission('system:permission:assign-user-role')")
    public CommonResult<Boolean> assignUserRole(@Validated @RequestBody PermissionAssignUserRoleReqVO reqVO) {
        // 带操作者越权校验
        Long operatorUserId = SecurityFrameworkUtils.getLoginUserId();
        permissionService.assignUserRole(operatorUserId, reqVO.getUserId(), reqVO.getRoleIds());
        return success(true);
    }

    @Operation(summary = "获得当前操作者可分配给角色的菜单编号集合")
    @GetMapping("/list-assignable-menus")
    @PreAuthorize("@ss.hasPermission('system:permission:assign-role-menu')")
    public CommonResult<Set<Long>> getAssignableMenuIds() {
        Long operatorUserId = SecurityFrameworkUtils.getLoginUserId();
        return success(permissionService.getAssignableMenuIdsByOperator(operatorUserId));
    }

}
