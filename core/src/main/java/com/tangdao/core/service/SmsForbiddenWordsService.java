package com.tangdao.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.tangdao.core.constant.SmsRedisConstant;
import com.tangdao.core.context.SettingsContext;
import com.tangdao.core.context.SettingsContext.DictType;
import com.tangdao.core.context.SettingsContext.WordsLibrary;
import com.tangdao.core.dao.SmsForbiddenWordsMapper;
import com.tangdao.core.model.domain.DictData;
import com.tangdao.core.model.domain.SmsForbiddenWords;
import com.tangdao.core.service.filter.SensitiveWordFilter;

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
public class SmsForbiddenWordsService extends BaseService<SmsForbiddenWordsMapper, SmsForbiddenWords> {

	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	
	@Autowired
	private DictDataService dictDataService;

	public boolean isContainsForbiddenWords(String content) {
		if (StrUtil.isEmpty(content)) {
			return false;
		}

		try {
			return SensitiveWordFilter.isContains(content);
		} catch (Exception e) {
			logger.error("解析是否包含敏感词失败", e);
			return false;
		}
	}

	public boolean isContainsForbiddenWords(String content, Set<String> safeWords) {
		Set<String> words = filterForbiddenWords(content);
		if (CollUtil.isEmpty(words)) {
			return false;
		}

		if (CollUtil.isEmpty(safeWords)) {
			return true;
		}

		// 如果报备的敏感词包含本次检索出来的敏感词，则认为本次无敏感词
		return !safeWords.containsAll(words);
	}

	public Set<String> filterForbiddenWords(String content) {
		if (StrUtil.isEmpty(content)) {
			return null;
		}

		// 过滤内容敏感词
		Set<String> sensitiveWords = SensitiveWordFilter.doFilter(content);

		// 过滤通配敏感词
		Set<String> wildcardsWords = SensitiveWordFilter.pickupWildcardsWords(content);
		if (CollUtil.isEmpty(sensitiveWords)) {
			if (CollUtil.isEmpty(wildcardsWords)) {
				return null;
			}

			return wildcardsWords;
		} else {
			if (CollUtil.isEmpty(wildcardsWords)) {
				return sensitiveWords;
			}

			sensitiveWords.addAll(wildcardsWords);
			return sensitiveWords;
		}
	}

	public Set<String> filterForbiddenWords(String content, Set<String> safeWords) {
		Set<String> set = filterForbiddenWords(content);
		if (CollUtil.isEmpty(set)) {
			return null;
		}
		set.removeAll(safeWords);
		return set;
	}

	public List<String> findForbiddenWordsLibrary() {
		try {
			Set<String> set = stringRedisTemplate.opsForSet().members(SmsRedisConstant.RED_FORBIDDEN_WORDS);
			if (CollUtil.isNotEmpty(set)) {
				return new ArrayList<>(set);
			}

		} catch (Exception e) {
			logger.warn("Redis敏感词加载失败.", e);
		}

		List<String> list = this.getBaseMapper().selectAllWords();
		try {
			stringRedisTemplate.execute((connection) -> {
				RedisSerializer<String> serializer = stringRedisTemplate.getStringSerializer();
				byte[] key = serializer.serialize(SmsRedisConstant.RED_FORBIDDEN_WORDS);
				connection.openPipeline();
				for (String mbl : list) {
					connection.sAdd(key, serializer.serialize(mbl));
				}
				return connection.closePipeline();
			}, false, true);

//            stringRedisTemplate.opsForSet().add(SmsRedisConstant.RED_FORBIDDEN_WORDS, list.toArray(new String[] {}));
		} catch (Exception e) {
			logger.warn("Redis敏感词同步失败.", e);
		}

		return list;
	}

	public boolean saveForbiddenWords(SmsForbiddenWords words) {
		if (words == null || StrUtil.isBlank(words.getWord()) || StrUtil.isBlank(words.getLabel())) {
			return false;
		}

		try {
			stringRedisTemplate.opsForSet().add(SmsRedisConstant.RED_FORBIDDEN_WORDS, words.getWord());
		} catch (Exception e) {
			logger.warn("Redis敏感词同步失败.", e);
		}

		// 暂时均默认为1级
		words.setLevel(1);
		boolean result = this.save(words);
		if (!result) {
			return false;
		}

		try {
			// 是否为通配敏感词
			if (SensitiveWordFilter.isWildcardsWords(words.getWord())) {
				SensitiveWordFilter.addWildcarsWords(words.getWord());
			} else {
				// 重新初始化明确敏感词词库(JVM)
				SensitiveWordFilter.setSensitiveWord(new ArrayList<>(findForbiddenWordsLibrary()));
			}
			return true;
		} catch (Exception e) {
			logger.error("保存敏感词[" + JSON.toJSONString(words) + "]失败", e);
			return false;
		}
	}

	public boolean reloadRedisForbiddenWords() {
		List<String> words = this.getBaseMapper().selectAllWords();
		if (CollUtil.isEmpty(words)) {
			logger.info("数据库未检索到敏感词，放弃填充REDIS");
			return true;
		}
		try {
			// 加载精确敏感词库
			SensitiveWordFilter.setSensitiveWord(words);

			// 加载通配敏感词库
			SensitiveWordFilter.loadWildcarsWords(words);

			stringRedisTemplate.delete(SmsRedisConstant.RED_FORBIDDEN_WORDS);
//            stringRedisTemplate.opsForSet().add(SmsRedisConstant.RED_FORBIDDEN_WORDS, words.toArray(new String[] {}));

			stringRedisTemplate.execute((connection) -> {
				RedisSerializer<String> serializer = stringRedisTemplate.getStringSerializer();
				byte[] key = serializer.serialize(SmsRedisConstant.RED_FORBIDDEN_WORDS);
				connection.openPipeline();
				for (String mbl : words) {
					connection.sAdd(key, serializer.serialize(mbl));
				}
				return connection.closePipeline();
			}, false, true);
			return true;
		} catch (Exception e) {
			logger.warn("REDIS重载敏感词数据失败", e);
			return false;
		}
	}

	public boolean deleteWord(int id) {
		try {
			SmsForbiddenWords words = super.getById(id);
			stringRedisTemplate.opsForSet().remove(SmsRedisConstant.RED_FORBIDDEN_WORDS, words.getWord());

			// 是否为通配敏感词
			if (SensitiveWordFilter.isWildcardsWords(words.getWord())) {
				SensitiveWordFilter.removeWildcarsWords(words.getWord());
			} else {
				// 重新初始化明确敏感词词库(JVM)
				SensitiveWordFilter.setSensitiveWord(new ArrayList<>(findForbiddenWordsLibrary()));
			}

		} catch (Exception e) {
			logger.warn("Redis 删除敏感词信息失败, id : {}", id, e);
			return false;
		}
		return super.removeById(id);
	}

	public String[] findWordsLabelLibrary() {
		DictData dictData =  dictDataService.getByDictTypeAndKey(DictType.WORDS_LIBRARY, WordsLibrary.FORBIDDEN_LABEL.name());
		if (dictData == null || StrUtil.isEmpty(dictData.getDictValue())) {
			logger.warn("敏感词标签库未配置，请及时配置");
			return null;
		}
		return dictData.getDictValue().split(SettingsContext.MULTI_VALUE_SEPERATOR);
	}

	public List<SmsForbiddenWords> getLabelByWords(String words) {
		if (StrUtil.isEmpty(words)) {
			return null;
		}

		String[] wordsArray = words.split(",");
		SmsForbiddenWords forbiddenWords = null;

		List<SmsForbiddenWords> list = new ArrayList<>();
		if (wordsArray.length == 1) {
			// 如果只有一个词汇，并且为空则直接返回空
			if (StrUtil.isBlank(wordsArray[0])) {
				return null;
			}
			forbiddenWords = this
					.getOne(Wrappers.<SmsForbiddenWords>lambdaQuery().eq(SmsForbiddenWords::getWord, wordsArray[0]));
			if (forbiddenWords == null) {
				return null;
			}

			list.add(forbiddenWords);
			return list;
		}
		List<SmsForbiddenWords> wordLib = this.list(
				Wrappers.<SmsForbiddenWords>lambdaQuery().in(SmsForbiddenWords::getWord, Lists.newArrayList(wordsArray)));
		if (CollUtil.isEmpty(wordLib)) {
			return null;
		}

		Map<String, SmsForbiddenWords> map = new HashMap<>();
		// 如果存在多个标签，需要判断是否是同一个，如果为同一个标签则只返回一个即可
		for (SmsForbiddenWords word : wordLib) {
			if (!map.containsKey(word.getLabel())) {
				map.put(word.getLabel(), word);
				continue;
			}

			SmsForbiddenWords originWord = map.get(word.getLabel());
			originWord.setWord(originWord.getWord() + "," + word.getWord());

			map.put(word.getLabel(), originWord);
		}

		for (String label : map.keySet()) {
			list.add(map.get(label));
		}

		return list;
	}

	public boolean freshLocalForbiddenWords(boolean isWildcards, boolean isSaveMode) {
		try {
			// 重新初始化明确敏感词词库(JVM)
			SensitiveWordFilter.setSensitiveWord(new ArrayList<>(findForbiddenWordsLibrary()));
			return true;
		} catch (Exception e) {
			logger.warn("REDIS刷新敏感词数据失败", e);
			return false;
		}
	}

}