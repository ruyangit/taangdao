package com.tangdao.core.model.domain.sms;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangdao.core.DataEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 通道控制Entity
 * 
 * @author ruyang
 * @version 2019-09-06
 */
@Getter
@Setter
@TableName("sms_passage_control")
public class PassageControl extends DataEntity<PassageControl> {

	private static final long serialVersionUID = 1L;

	@TableId
	private String id;

	private String passageId; // 通道ID
	private String cron; // 轮训表达式
	private String parameterId; // 通道参数ID

	public PassageControl() {
		super();
	}

}