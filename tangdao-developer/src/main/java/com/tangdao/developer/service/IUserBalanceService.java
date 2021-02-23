/**
 *
 */
package com.tangdao.developer.service;

import com.tangdao.core.constant.CommonContext.PlatformType;

/**
 * <p>
 * TODO 描述
 * </p>
 *
 * @author ruyang
 * @since 2021年2月23日
 */
public interface IUserBalanceService {

	public int calculateSmsAmount(String userCode, String content);

	public boolean isBalanceEnough(String userCode, PlatformType platformType, double totalFee);
}
