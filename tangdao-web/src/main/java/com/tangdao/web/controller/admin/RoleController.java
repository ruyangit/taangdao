/**
 *
 */
package com.tangdao.web.controller.admin;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tangdao.common.CommonResponse;
import com.tangdao.common.constant.CommonApiCode;
import com.tangdao.common.exception.BusinessException;
import com.tangdao.core.constant.DataStatus;
import com.tangdao.core.web.BaseController;
import com.tangdao.core.web.validate.Field;
import com.tangdao.core.web.validate.Rule;
import com.tangdao.core.web.validate.Validate;
import com.tangdao.model.domain.Role;
import com.tangdao.model.dto.PolicyDTO;
import com.tangdao.model.dto.RoleDTO;
import com.tangdao.model.dto.RoleMenuDTO;
import com.tangdao.modules.sys.service.MenuService;
import com.tangdao.modules.sys.service.RoleService;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

/**
 * <p>
 * TODO 描述
 * </p>
 *
 * @author ruyang@gmail.com
 * @since 2020年6月5日
 */
@RestController
@RequestMapping(value = { "/admin/roles" })
public class RoleController extends BaseController {

	@Autowired
	private RoleService roleService;

	@Autowired
	private MenuService menuService;
	
	@GetMapping
//	@PreAuthorize("hasAuthority('admin:roles:GET')")
	public CommonResponse page(Page<Role> page, String roleName) {
		QueryWrapper<Role> queryWrapper = new QueryWrapper<Role>();
		if (StrUtil.isNotBlank(roleName)) {
			queryWrapper.like("role_name", roleName);
		}
		return success(roleService.page(page, queryWrapper));
	}

	@GetMapping("/list")
	public CommonResponse list(String roleName) {
		QueryWrapper<Role> queryWrapper = new QueryWrapper<Role>();
		if (StrUtil.isNotBlank(roleName)) {
			queryWrapper.like("role_name", roleName);
		}
		queryWrapper.eq("status", DataStatus.NORMAL);
		return success(roleService.list(queryWrapper));
	}

	@Validate({ @Field(name = "id", rules = { @Rule(message = "查询主键不能为空") }) })
	@GetMapping("/detail")
	public CommonResponse detail(String id) {
		Role role = roleService.getById(id);
		Map<String, Object> data = MapUtil.newHashMap();
		data.put("role", role);

		List<String> menuIds = CollUtil.getFieldValues(menuService.findRoleMenuList(id), "id", String.class);
		data.put("menuIds", menuIds);
		return success(data);
	}

	@Validate({ @Field(name = "role.roleName", rules = { @Rule(message = "角色名不能为空") }) })
	@PostMapping
	public CommonResponse saveRole(@RequestBody Role role) {
		Role er = roleService.findByRoleName(role.getRoleName());
		if (er != null) {
			throw new IllegalArgumentException("角色 '" + er.getRoleName() + "' 已存在");
		}
		return success(roleService.save(role));
	}
	
	@PostMapping("/menu")
	public CommonResponse saveRoleMenu(@RequestBody RoleMenuDTO roleMenuDTO) {
		return success(roleService.saveRoleMenu(roleMenuDTO));
	}

	@PostMapping("/update")
	public CommonResponse updateRole(@RequestBody RoleDTO roleDto) {
		Role role = new Role();
		BeanUtil.copyProperties(roleDto, role);

		if (!Validator.equal(roleDto.getRoleName(), roleDto.getOldRoleName())
				&& roleService.count(Wrappers.<Role>lambdaQuery().eq(Role::getRoleName, roleDto.getRoleName())) > 0) {
			throw new IllegalArgumentException("角色 '" + roleDto.getRoleName() + "' 已存在");
		}

		if (roleService.checkUserRoleRef(roleDto)) {
			throw new BusinessException(CommonApiCode.FAIL, "操作失败，存在未解除的关联数据");
		}

		return success(roleService.updateById(role));
	}

	@PostMapping("/delete")
	public CommonResponse deleteRole(@RequestBody RoleDTO roleDto) {
		if (roleService.checkUserRoleRef(roleDto)) {
			throw new BusinessException(CommonApiCode.FAIL, "操作失败，存在未解除的关联数据");
		}
		return success(roleService.deleteRole(roleDto.getId()));
	}
	
	@GetMapping("/policies")
	public CommonResponse policies(String roleId) {
		return success(roleService.findRolePolicy(roleId));
	}
	
	@PostMapping("/policies")
	public CommonResponse policies(@RequestBody PolicyDTO policyDTO) {
		return success(roleService.saveRolePolicy(policyDTO));
	}
}