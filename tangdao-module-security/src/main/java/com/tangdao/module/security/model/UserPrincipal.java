/**
 * 
 */
package com.tangdao.module.security.model;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tangdao.framework.model.UserInfo;

/**
 * <p>
 * TODO 用户授权信息
 * </p>
 *
 * @author ruyangit@gmail.com
 * @since 2020年2月22日
 */
public class UserPrincipal implements UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 用户信息
	 */
	private UserInfo user;
	
	/**
	 * 
	 * @param user
	 */
	public UserPrincipal(UserInfo user) {
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Set<SimpleGrantedAuthority> collect = user.getRoles().stream()
				.map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
				.collect(Collectors.toSet());
		return collect;
	}

	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	
	@Override
	@JsonIgnore
	public String getPassword() {
		// TODO Auto-generated method stub
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return user.getLoginName();
	}

	/**
	 * @return the user
	 */
	public UserInfo getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public UserPrincipal setUser(UserInfo user) {
		this.user = user;
		return this;
	}
	
}