package com.tangdao.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tangdao.core.constant.SmsRedisConstant;
import com.tangdao.core.dao.SmsMobileWhitelistMapper;
import com.tangdao.core.model.domain.SmsMobileWhitelist;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 
 * <p>
 * TODO 描述
 * </p>
 *
 * @author ruyang
 * @since 2021年3月10日
 */
public class SmsMobileWhitelistService extends BaseService<SmsMobileWhitelistMapper, SmsMobileWhitelist> {

	@Autowired
	private SmsMobileWhitelistMapper smsMobileWhitelistMapper;

	@Resource
	private StringRedisTemplate stringRedisTemplate;

	private static Map<String, Object> response(String code, String msg) {
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("result_code", code);
		resultMap.put("result_msg", msg);
		return resultMap;
	}

	public Map<String, Object> batchInsert(SmsMobileWhitelist white) {
		if (StrUtil.isEmpty(white.getMobile())) {
			return response("-2", "参数不能为空！");
		}

		List<SmsMobileWhitelist> list = new ArrayList<>();
		try {
			// 前台默认是多个手机号码换行添加
			String[] mobiles = white.getMobile().split("\n");
			SmsMobileWhitelist mwl;
			for (String mobile : mobiles) {
				if (StrUtil.isBlank(mobile)) {
					continue;
				}

				// 判断是否重复 重复则不保存
				int statCount = selectByUserIdAndMobile(white.getUserId(), mobile.trim());
				if (statCount > 0) {
					continue;
				}

				mwl = new SmsMobileWhitelist();
				mwl.setMobile(mobile.trim());
				mwl.setUserId(white.getUserId());

				list.add(mwl);
			}

			if (CollUtil.isNotEmpty(list)) {
				this.saveBatch(list);

				// 批量操作无误后添加至缓存REDIS
				for (SmsMobileWhitelist ml : list) {
					pushToRedis(ml);
				}
			}

			return response("success", "成功！");
		} catch (Exception e) {
			logger.info("添加白名单失败", e);
			return response("exption", "操作失败");
		}
	}

	public List<SmsMobileWhitelist> selectByUserId(String userId) {
		return this.list(Wrappers.<SmsMobileWhitelist>lambdaQuery().eq(SmsMobileWhitelist::getUserId, userId));
	}

	/**
	 * 获取白名单手机号码KEY名称
	 *
	 * @param userId 用户ID
	 * @return key
	 */
	private String getKey(String userId) {
		return String.format("%s:%s", SmsRedisConstant.RED_MOBILE_WHITELIST, userId);
	}

	public boolean reloadToRedis() {
		List<SmsMobileWhitelist> list = this.list();
		if (CollUtil.isEmpty(list)) {
			logger.info("数据库未检索到手机白名单，放弃填充REDIS");
			return true;
		}
		try {
			stringRedisTemplate.delete(stringRedisTemplate.keys(SmsRedisConstant.RED_MOBILE_WHITELIST + "*"));

			List<Object> con = stringRedisTemplate.execute((connection) -> {

				RedisSerializer<String> serializer = stringRedisTemplate.getStringSerializer();
				connection.openPipeline();
				for (SmsMobileWhitelist mwl : list) {
					byte[] key = serializer.serialize(getKey(mwl.getUserId()));

					connection.sAdd(key, serializer.serialize(mwl.getMobile()));
				}

				return connection.closePipeline();

			}, false, true);

			return CollUtil.isNotEmpty(con);
		} catch (Exception e) {
			logger.warn("REDIS重载手机白名单数据失败", e);
			return false;
		}
	}

	/**
	 * 添加到REDIS 数据中
	 * 
	 * @param mwl 手机白名单数据
	 */
	private void pushToRedis(SmsMobileWhitelist mwl) {
		try {
			stringRedisTemplate.opsForSet().add(getKey(mwl.getUserId()), mwl.getMobile());
		} catch (Exception e) {
			logger.error("REDIS加载手机白名单信息", e);
		}
	}

	public boolean isMobileWhitelist(String userId, String mobile) {
		if (StrUtil.isEmpty(userId) || StrUtil.isEmpty(mobile)) {
			return false;
		}

		try {
			return stringRedisTemplate.opsForSet().isMember(getKey(userId), mobile);
		} catch (Exception e) {
			logger.warn("REDIS 获取手机号码白名单失败，将从DB加载", e);
			return selectByUserIdAndMobile(userId, mobile) > 0;
		}
	}

	public Set<String> getByUserId(String userId) {
		try {
			return stringRedisTemplate.opsForSet().members(getKey(userId));
		} catch (Exception e) {
			logger.warn("REDIS 获取手机号码白名单集合失败，将从DB加载", e);
			List<String> list = smsMobileWhitelistMapper.selectDistinctMobilesByUserId(userId);
			if (CollUtil.isEmpty(list)) {
				return null;
			}

			return new HashSet<>(list);
		}
	}

	public int selectByUserIdAndMobile(String userId, String mobile) {
		QueryWrapper<SmsMobileWhitelist> queryWrapper = new QueryWrapper<SmsMobileWhitelist>();
		queryWrapper.eq("user_id", userId);
		queryWrapper.eq("mobile", mobile);
		return count(queryWrapper);
	}

}