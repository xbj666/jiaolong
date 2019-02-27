package com.zbf.service;

import com.zbf.core.page.Page;
import com.zbf.core.utils.UID;
import com.zbf.entity.RedisKey;
import com.zbf.mapper.TestPaperMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 1.0
 * @date 2019/2/21 21:11
 */
@Component
public class TestPaperService {

    @Autowired
    private TestPaperMapper testPaperMapper;

    /**
     * 添加试卷分类
     *
     * @param parameterMap
     * @param redisTemplate
     */
    @Transactional
    public void addFenlei(Map<String, Object> parameterMap, RedisTemplate redisTemplate) {
        //插入 fenleiid
        parameterMap.put("fenleiid", UID.getUUIDOrder());
        testPaperMapper.addFenlei(parameterMap);
        redisTemplate.opsForHash().put(RedisKey.SHIJUANFENLEI, parameterMap.get("fenleiid").toString(), parameterMap);
    }

    /**
     * @param page
     * @param redisTemplate
     * @return
     */
    public void fenleiList(Page<Map<String, Object>> page, RedisTemplate redisTemplate) {

        List<Map<String, Object>> list = redisTemplate.opsForHash().values(RedisKey.SHIJUANFENLEI);

        // 分页
        int pageSize = page.getPageSize();
        int pageNo = page.getPageNo();
        int pageStart = (pageNo - 1) * pageSize;
        page.setTotalCount(list.size());

        // public List<E> subList(int fromIndex,int toIndex) 0,10 10,20
        List<Map<String, Object>> subList = null;
        if (pageStart + pageSize <= page.getTotalCount()) {
            subList = list.subList(pageStart, pageStart + pageSize);
        } else {
            subList = list.subList(pageStart, page.getTotalCount());
        }

        page.setResultList(subList);
    }

    public List<Map<String, Object>> getFenlei(RedisTemplate redisTemplate) {

        List<Map<String, Object>> list = redisTemplate.opsForHash().values(RedisKey.SHIJUANFENLEI);

        return list;
    }

    public void addshijuan(Map<String,Object> parameterMap) {
    }

    public void userInfo(Page<Map<String,Object>> page) {

        List<Map<String, Object>> list = testPaperMapper.userInfo(page);

        page.setResultList(list);
    }
}
