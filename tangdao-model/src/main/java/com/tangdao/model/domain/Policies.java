/**
 *
 */
package com.tangdao.model.domain;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tangdao.model.base.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * TODO 描述
 * </p>
 *
 * @author ruyang@gmail.com
 * @since 2020年7月7日
 */
@Getter
@Setter
@TableName("policy")
public class Policies extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String policyName;
	
	private String policyNameCn;
	
	private String policyType;
	
	private String content;
	
	private String version;
	
	private String remark;
	
	private String status;
	
	private Date modified;
}
