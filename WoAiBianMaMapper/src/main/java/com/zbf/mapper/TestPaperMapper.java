package com.zbf.mapper;

import com.zbf.core.page.Page;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author chuck
 * @version 1.0
 * @date 2019/2/21 21:12
 */
@Mapper
public interface TestPaperMapper {

    /**
     * 添加试卷分类
     * @param parameterMap
     */
    void addFenlei(Map<String, Object> parameterMap);

    List<Map<String,Object>> userInfo(Page<Map<String,Object>> page);
}
