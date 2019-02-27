package com.zbf.service;

import com.alibaba.fastjson.JSONArray;
import com.zbf.core.page.Page;
import com.zbf.core.utils.AESUtils;
import com.zbf.core.utils.UID;
import com.zbf.entity.Question;
import com.zbf.entity.RedisKey;
import com.zbf.mapper.QuestionBankMapper;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chuck
 * @version 1.0
 * @date 2019/2/15 8:55
 */
@Component
public class QuestionBankService {

    @Autowired
    private QuestionBankMapper questionBankMapper;


    /**
     * 题库添加
     *
     * @param map
     * @param redisTemplate
     */
    @Transactional
    public void toAddQuestion(Map<String, Object> map, RedisTemplate redisTemplate) {
        map.put("id", UID.getUUIDOrder());
        questionBankMapper.toAddQuestion(map);

        //存入Redis Hash结构中 方便修改
        redisTemplate.opsForHash().put(RedisKey.QUESTIONBANK, map.get("id").toString(), map);

        //redisTemplate.opsForList().rightPush(RedisKey.QUESTIONBANK, map);
    }

    /**
     * 题库列表
     *
     * @param page
     */
    public void toAddQuestionList(Page<Map<String, Object>> page) {
        List<Map<String, Object>> list = questionBankMapper.findQuestionList(page);
        //处理 题库状态
        list.forEach(map -> {
            if ("1".equals(map.get("tikuzhuangtai").toString())) {
                map.put("tikuzhuangtai", "开放");
            } else {
                map.put("tikuzhuangtai", "关闭");
            }
            String time = map.get("createtime").toString().substring(0, map.get("createtime").toString().lastIndexOf("."));
            map.put("createtime", time);
        });

        page.setResultList(list);
    }

    /**
     * 更新题库信息
     *
     * @param paramsJsonMap
     * @param redisTemplate
     */
    @Transactional
    public void updateQuestionBankInfo(Map<String, Object> paramsJsonMap, RedisTemplate redisTemplate) {
        questionBankMapper.updateQuestionBankInfo(paramsJsonMap);
        redisTemplate.opsForHash().put(RedisKey.QUESTIONBANK, paramsJsonMap.get("id").toString(), paramsJsonMap);
    }

    /**
     * 从Redis中获取题库列表
     *
     * @param redisTemplate
     * @return
     */
    public List<Map<String, Object>> getQuestionBank(RedisTemplate redisTemplate) {
        //List<Map<String, Object>> range = redisTemplate.opsForList().range(RedisKey.QUESTIONBANK, 0, -1);


        List<Map<String, Object>> values = redisTemplate.opsForHash().values(RedisKey.QUESTIONBANK);

        return values;
    }

    /**
     * 添加试题
     *
     * @param paramsJsonMap
     */
    public void insertQuestion(Map<String, Object> paramsJsonMap) throws Exception {
        //添加试题id
        paramsJsonMap.put("id", UID.getUUIDOrder());
        //组装答案
        String inputValue = JSONArray.toJSONString(paramsJsonMap.getOrDefault("inputValue", "null"));
        String checked = JSONArray.toJSONString(paramsJsonMap.getOrDefault("checked", "null"));
        StringBuilder daan = new StringBuilder("{\"inputValue\":");
        /*
            答案格式
            {"inputValue":
            ["答案A","答案B","答案C","答案D"]
            ,"checked":
            ["C","D"]
            }
             */
        daan.append(inputValue).append(",\"checked\":").append(checked).append("}");
        paramsJsonMap.put("daan", daan.toString());

        //解密题干和试题解析
        String tigan = paramsJsonMap.getOrDefault("tigan", "").toString();
        tigan = AESUtils.desEncrypt(tigan, "8a7cd3044f354684", "8a7cd3044f354684");
        paramsJsonMap.put("tigan", tigan);
        String jiexi = paramsJsonMap.getOrDefault("jiexi", "").toString();
        jiexi = AESUtils.desEncrypt(jiexi, "8a7cd3044f354684", "8a7cd3044f354684");
        paramsJsonMap.put("jiexi", jiexi);

        questionBankMapper.insertQuestion(paramsJsonMap);
    }

    /**
     * 试题管理页面
     * Solr 高亮查询试题
     *
     * @param page
     * @param client
     */
    public void questionlist(Page<Question> page, SolrClient client) throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery();
        /*questionlist:题库
        AND tikuid:1000000188419766
        AND tixingid:1
        AND shitizhuangtai:1
        AND nanduid:4*/
        StringBuilder sb = new StringBuilder();
        sb.append("questionlist:");
        //开启高亮
        if (page.getParams().get("questionlist") != null && !"".equals(page.getParams().get("questionlist"))) {
            query.setHighlight(true);
            //设置高亮字段
            query.addHighlightField("userName");
            query.addHighlightField("tikuname");
            query.addHighlightField("createtime");
            query.addHighlightField("tigan");

            query.setHighlightSimplePre("<font color='red'>"); // ⾼亮单词的前缀
            query.setHighlightSimplePost("</font>"); // ⾼亮单词的后缀

            sb.append(page.getParams().getOrDefault("questionlist", "*").toString());
            if (page.getParams().get("tikuid") != null) {
                sb.append(" AND ").append("tikuid:").append(page.getParams().get("tikuid").toString());
            }
            if (page.getParams().get("tixingid") != null) {
                sb.append(" AND ").append("tixingid:").append(page.getParams().get("tixingid").toString());
            }
            if (page.getParams().get("shitizhuangtai") != null) {
                sb.append(" AND ").append("shitizhuangtai:").append(page.getParams().get("shitizhuangtai").toString());
            }
            if (page.getParams().get("nanduid") != null) {
                sb.append(" AND ").append("nanduid:").append(page.getParams().get("nanduid").toString());
            }
            if (page.getParams().get("order") != null) {
                query.set("sort", page.getParams().get("order").toString());
            }
            query.setQuery(sb.toString());

            //分页
            query.setStart(page.getPageSize() * (page.getPageNo() - 1));
            query.setRows(page.getPageSize());

            QueryResponse response = client.query(query);
            //获取总条数
            long numFound = response.getResults().getNumFound();
            page.setTotalCount((int) numFound);


            //获取高亮结果
            Map<String, Map<String, List<String>>> highlighting = response.getHighlighting();
            //获取查询结果
            List<Question> beans = response.getBeans(Question.class);

            beans.forEach(item -> {
                if (highlighting.get(item.getId()) != null) {
                    Map<String, List<String>> stringListMap = highlighting.get(item.getId());
                    if (stringListMap.get("tikuname") != null) {
                        List<String> tikuname = stringListMap.get("tikuname");
                        item.setTikuname(tikuname.get(0));
                    }
                    if (stringListMap.get("userName") != null) {
                        List<String> userName = stringListMap.get("userName");
                        item.setUserName(userName.get(0));
                    }
                    if (stringListMap.get("tigan") != null) {
                        List<String> tigan = stringListMap.get("tigan");
                        item.setTigan(tigan.get(0));
                    }

                }
            });
            page.setResultList(beans);
        } else {
            sb.append(page.getParams().getOrDefault("questionlist", "*").toString());
            if (page.getParams().get("tikuid") != null) {
                sb.append(" AND ").append("tikuid:").append(page.getParams().get("tikuid").toString());
            }
            if (page.getParams().get("tixingid") != null) {
                sb.append(" AND ").append("tixingid:").append(page.getParams().get("tixingid").toString());
            }
            if (page.getParams().get("shitizhuangtai") != null) {
                sb.append(" AND ").append("shitizhuangtai:").append(page.getParams().get("shitizhuangtai").toString());
            }
            if (page.getParams().get("nanduid") != null) {
                sb.append(" AND ").append("nanduid:").append(page.getParams().get("nanduid").toString());
            }
            if (page.getParams().get("order") != null) {
                query.set("sort", page.getParams().get("order").toString());
            }
            query.setQuery(sb.toString());


            //分页
            query.setStart(page.getPageSize() * (page.getPageNo() - 1));
            query.setRows(page.getPageSize());

            QueryResponse response = client.query(query);
            //获取总条数
            long numFound = response.getResults().getNumFound();
            page.setTotalCount((int) numFound);

            List<Question> beans = response.getBeans(Question.class);
            page.setResultList(beans);
        }

    }

    /**
     * Excel文件上传 导入数据
     *
     * @param parameterMap
     * @param listdata
     */
    public void questionBatchImport(Map<String, Object> parameterMap, List<Map<String, Object>> listdata) {
        //form表单数据
        String userid = parameterMap.get("userid").toString();
        String nanduid = parameterMap.get("nanduid").toString();
        String tikuid = parameterMap.get("tikuid").toString();
        String laiyuan = parameterMap.get("laiyuan").toString();
        String tixingid = parameterMap.get("tixingid").toString();
        String shitizhuangtai = parameterMap.get("shitizhuangtai").toString();
        Map<String, Object> map = new HashMap<>(16);
        listdata.forEach(item -> {
            //封装数据
            //试题id
            map.put("id", UID.getUUIDOrder());
            map.put("userid", userid);
            map.put("nanduid", nanduid);
            map.put("tikuid", tikuid);
            map.put("laiyuan", laiyuan);
            map.put("tixingid", tixingid);
            map.put("shitizhuangtai", shitizhuangtai);
            map.put("tigan", item.get("tigan").toString());
            map.put("jiexi", item.getOrDefault("jiexi", ""));
            /*
            答案格式
            {"inputValue":
            ["答案A","答案B","答案C","答案D"]
            ,"checked":
            ["C","D"]
            }
             */
            StringBuilder sb = new StringBuilder();

            String xuanxiangmiaoshu = item.get("xuanxiangmiaoshu").toString();
            String daan = item.get("daan").toString();
            sb.append("{\"inputValue\":");
            sb.append(xuanxiangmiaoshu);
            sb.append(",\"checked\":");
            sb.append(daan);
            map.put("daan",sb.toString());
            questionBankMapper.insertQuestion(map);
        });


    }
}
