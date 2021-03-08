package com.tangdao.core.model.domain.paas;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangdao.core.DataEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 行政区划Entity
 * 
 * @author ruyang
 * @version 2019-09-27
 */
@Getter
@Setter
@TableName("paas_area")
public class Area extends DataEntity<Area> {

	private static final long serialVersionUID = 1L;

	@TableId
	private String areaCode; // 区域编码
	private String areaName; // 区域名称
	private String areaType; // 区域类型

	public static final Integer AREA_CODE_ALLOVER_COUNTRY = 0;

	public Area() {
		super();
	}
}