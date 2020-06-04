/**
 *
 */
package com.tangdao.modules.user.service;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tangdao.model.Role;
import com.tangdao.modules.user.mapper.RoleMapper;

/**
 * <p>
 * TODO 描述
 * </p>
 *
 * @author ruyang@gmail.com
 * @since 2020年6月2日
 */
@Service
public class RoleService extends ServiceImpl<RoleMapper, Role> implements IService<Role> {
	
	public static final String GLOBAL_ADMIN_ROLE = "ROLE_ADMIN";

}
