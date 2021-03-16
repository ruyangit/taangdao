/**
 *
 */
package com.tangdao.portal.web.security;

import java.util.Date;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import com.tangdao.core.context.SessionContext;
import com.tangdao.core.model.vo.SessionUser;
import com.tangdao.portal.web.security.model.AuthUser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * <p>
 * TODO 描述
 * </p>
 *
 * @author ruyang
 * @since 2021年3月12日
 */
@Component
public class JwtTokenProvider {

	private static final String AUTHORITIES_KEY = "JWT-TOKEN-AUTH";

	private static final String SECRET_KEY = "SECRET";

	private static final Long SECRET_EXPIRATION = 18000L;

	/**
	 * Create token
	 *
	 * @param authentication
	 * @return token
	 */
	public String createToken(Authentication authentication) {
		long now = (new Date()).getTime();

		Date validity = new Date(now + SECRET_EXPIRATION * 1000L);

		Claims claims = Jwts.claims().setSubject(authentication.getName());

		AuthUser authUser = (AuthUser) authentication.getPrincipal();
		claims.put("id", authUser.getId());
		return Jwts.builder().setClaims(claims).setExpiration(validity).signWith(SignatureAlgorithm.HS256, SECRET_KEY)
				.compact();
	}

	/**
	 * Get auth Info
	 *
	 * @param token
	 * @return auth
	 */
	public Authentication getAuthentication(String token) {
		Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();

		List<GrantedAuthority> authorities = AuthorityUtils
				.commaSeparatedStringToAuthorityList((String) claims.get(AUTHORITIES_KEY));

		AuthUser principal = new AuthUser();
		principal.setId((String) claims.get("id"));
		principal.setUsername(claims.getSubject());
		principal.setToken(token);
		return new UsernamePasswordAuthenticationToken(principal, "", authorities);
	}

	/**
	 * validate token
	 *
	 * @param token token
	 * @return whether valid
	 */
	public void validateToken(String token) {
		Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
	}

	/**
	 * 
	 * TODO
	 * 
	 * @param authentication
	 */
	public void putSessionContext(Authentication authentication) {
		AuthUser authUser = (AuthUser) authentication.getPrincipal();
		//
		SessionUser session = new SessionUser();
		session.setId(authUser.getId());
		session.setUsername(authUser.getUsername());
		SessionContext.setSession(session);
	}
}