/**
 *
 */
package com.tangdao.modules.user.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tangdao.model.User;
import com.tangdao.modules.user.mapper.UserMapper;

import cn.hutool.core.collection.CollUtil;

/**
 * <p>
 * TODO 描述
 * </p>
 *
 * @author ruyang@gmail.com
 * @since 2020年5月28日
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User> implements IService<User> {

	public User findUserByUsername(String username) {
		return findUser(this.list(Wrappers.<User>lambdaQuery().eq(User::getUsername, username)));
	}
	
	public User findUserByMobile(String mobile) {
		return findUser(this.list(Wrappers.<User>lambdaQuery().eq(User::getMobile, mobile)));
	}
	
	public User findUserById(String id) {
		return findUser(this.list(Wrappers.<User>lambdaQuery().eq(User::getId, id)));
	}

	public User findUser(List<User> users) {
		if (CollUtil.isNotEmpty(users)) {
			return users.get(0);
		}
		return null;
	}
	
	public void createUser(String username, String password) {
		User user = new User();
		user.setUsername(username);
		user.setPassword(password);
		user.setCreated(new Date());
		this.save(user);
	}
}
