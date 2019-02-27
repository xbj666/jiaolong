package com.zbf.mapper;

import com.zbf.core.page.Page;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author chuck
 * @version 1.0
 * @date 2019/2/15 8:43
 */
@Mapper
public interface QuestionBankMapper {

    /**
     * 题库添加
     * @param map
     */
    void toAddQuestion(Map<String,Object> map);

    /**
     * 题库列表
     * @param page
     * @return
     */
    List<Map<String,Object>> findQuestionList(Page<Map<String,Object>> page);

    /**
     * 更新题库信息
     * @param paramsJsonMap
     */
    void updateQuestionBankInfo(Map<String, Object> paramsJsonMap);

    /**
     * 添加试题
     * @param paramsJsonMap
     */
    void insertQuestion(Map<String, Object> paramsJsonMap);
}
