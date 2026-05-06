package com.example.exam_system.examsession.service;

import com.example.exam_system.examsession.dto.ExamAnswerCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisAnswerCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisAnswerCacheService.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "exam:answer:";
    private static final String STATUS_KEY_PREFIX = "exam:status:";
    private static final long TTL_HOURS = 2;

    public void initCache(ExamAnswerCache cache) {
        String key = KEY_PREFIX + cache.getSessionToken();
        try {
            cache.setLastUpdateTime(System.currentTimeMillis());
            String json = objectMapper.writeValueAsString(cache);
            redisTemplate.opsForValue().set(key, json, TTL_HOURS, TimeUnit.HOURS);

            String statusKey = STATUS_KEY_PREFIX + cache.getSessionToken();
            redisTemplate.opsForValue().set(statusKey, cache.getStatus(), TTL_HOURS, TimeUnit.HOURS);

            log.info("初始化考试缓存，sessionId: {}, status: {}", cache.getSessionId(), cache.getStatus());
        } catch (JsonProcessingException e) {
            log.error("缓存序列化失败", e);
            throw new RuntimeException("缓存初始化失败");
        }
    }

    public ExamAnswerCache getCache(String sessionToken) {
        String key = KEY_PREFIX + sessionToken;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) return null;

        try {
            return objectMapper.readValue(json, ExamAnswerCache.class);
        } catch (JsonProcessingException e) {
            log.error("缓存反序列化失败", e);
            return null;
        }
    }

    public void updateAnswer(String sessionToken, Long questionId, String studentAnswer) {
        ExamAnswerCache cache = getCache(sessionToken);
        if (cache != null) {
            cache.getAnswers().put(questionId, studentAnswer);
            cache.setLastUpdateTime(System.currentTimeMillis());
            initCache(cache);
            log.debug("更新 Redis 缓存答案，sessionId: {}, questionId: {}", cache.getSessionId(), questionId);
        }
    }

    public void batchUpdateAnswers(String sessionToken, java.util.Map<Long, String> answers) {
        ExamAnswerCache cache = getCache(sessionToken);
        if (cache != null) {
            cache.getAnswers().putAll(answers);
            cache.setLastUpdateTime(System.currentTimeMillis());
            initCache(cache);
            log.info("批量更新 Redis 缓存答案，sessionId: {}, 题目数：{}", cache.getSessionId(), answers.size());
        }
    }

    public void updateExamStatus(String sessionToken, String status) {
        String statusKey = STATUS_KEY_PREFIX + sessionToken;
        redisTemplate.opsForValue().set(statusKey, status, TTL_HOURS, TimeUnit.HOURS);

        ExamAnswerCache cache = getCache(sessionToken);
        if (cache != null) {
            cache.setStatus(status);
            initCache(cache);
        }

        log.info("更新考试状态为：{}, sessionToken: {}", status, sessionToken);
    }

    public String getExamStatus(String sessionToken) {
        String statusKey = STATUS_KEY_PREFIX + sessionToken;
        return redisTemplate.opsForValue().get(statusKey);
    }

    public void deleteCache(String sessionToken) {
        String key = KEY_PREFIX + sessionToken;
        String statusKey = STATUS_KEY_PREFIX + sessionToken;
        redisTemplate.delete(key);
        redisTemplate.delete(statusKey);
        log.info("删除考试缓存，sessionToken: {}", sessionToken);
    }

    public boolean hasCache(String sessionToken) {
        String key = KEY_PREFIX + sessionToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
