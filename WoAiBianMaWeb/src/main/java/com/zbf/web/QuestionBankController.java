package com.zbf.web;

import com.alibaba.fastjson.JSON;
import com.zbf.common.ResponseResult;
import com.zbf.core.CommonUtils;
import com.zbf.core.page.Page;
import com.zbf.entity.Question;
import com.zbf.service.QuestionBankService;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 1.0
 * @date 2019/2/15 8:40
 */
@RequestMapping("/questionbank")
@RestController
public class QuestionBankController {

    @Autowired
    private QuestionBankService questionBankService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SolrClient client;

    /**
     * 题库添加
     *
     * @param request
     * @return
     */
    @RequestMapping("/toAddQuestion")
    public ResponseResult toAddQuestion(HttpServletRequest request) {

        Map<String, Object> map = CommonUtils.getParamsJsonMap(request);
        ResponseResult responseResult = ResponseResult.getResponseResult();

        try {
            questionBankService.toAddQuestion(map, redisTemplate);
            responseResult.setSuccess("ok");
        } catch (Exception e) {
            e.printStackTrace();
            //简单回滚
//            redisTemplate.opsForList().rightPop(RedisKey.QUESTIONBANK);
            responseResult.setError("error");
        }
        return responseResult;
    }

    /**
     * 题库列表
     *
     * @param request
     * @return
     */
    @RequestMapping("/getQuestionList")
    public ResponseResult getQuestionList(HttpServletRequest request) {

        Map<String, Object> parameterMap = CommonUtils.getParamsJsonMap(request);
        ResponseResult responseResult = ResponseResult.getResponseResult();
        Page<Map<String, Object>> page = new Page<>();
        //设置查询参数
        page.setParams(parameterMap);
        Page.setPageInfo(page, parameterMap);
        questionBankService.toAddQuestionList(page);
        responseResult.setResult(page);

        return responseResult;
    }

    /**
     * 更新题库信息
     *
     * @param request
     * @return
     */
    @RequestMapping("/updateQuestionBankInfo")
    public ResponseResult updateQuestionBankInfo(HttpServletRequest request) {

        Map<String, Object> paramsJsonMap = CommonUtils.getParamsJsonMap(request);
        ResponseResult responseResult = ResponseResult.getResponseResult();

        try {
            questionBankService.updateQuestionBankInfo(paramsJsonMap, redisTemplate);
            responseResult.setSuccess("ok");
        } catch (Exception e) {
            e.printStackTrace();
            responseResult.setError("error");
        }

        return responseResult;
    }

    /**
     * 从Redis中获取题库列表
     *
     * @return
     */
    @RequestMapping("/getQuestionBank")
    public ResponseResult getQuestionBank() {
        ResponseResult responseResult = ResponseResult.getResponseResult();
        try {
            List<Map<String, Object>> list = questionBankService.getQuestionBank(redisTemplate);
            responseResult.setResult(list);
            responseResult.setSuccess("ok");
        } catch (Exception e) {
            e.printStackTrace();
            responseResult.setError("error");
        }
        return responseResult;
    }

    /**
     * 添加试题
     *
     * @param request
     * @return
     */
    @RequestMapping("/insertQuestion")
    public ResponseResult insertQuestion(HttpServletRequest request) {
        Map<String, Object> paramsJsonMap = CommonUtils.getParamsJsonMap(request);
        ResponseResult responseResult = ResponseResult.getResponseResult();
        try {
            questionBankService.insertQuestion(paramsJsonMap);
            responseResult.setSuccess("ok");
        } catch (Exception e) {
            e.printStackTrace();
            responseResult.setError("error");
        }

        return responseResult;
    }

    /**
     * 试题管理页面
     * Solr 高亮查询试题
     *
     * @param request
     * @return
     */
    @RequestMapping("/questionlist")
    public ResponseResult questionlist(HttpServletRequest request) throws IOException, SolrServerException {
        Map<String, Object> paramsJsonMap = CommonUtils.getParamsJsonMap(request);
        ResponseResult responseResult = ResponseResult.getResponseResult();
        Page<Question> page = new Page<>();

        Page.setPageInfo(page, paramsJsonMap);
        questionBankService.questionlist(page, client);

        responseResult.setResult(page);

        return responseResult;
    }

    /**
     * Solr数据删除按钮
     *
     * @return
     */
    @RequestMapping("/questionlistDeleteAll")
    public ResponseResult questionlistDeleteAll() throws IOException, SolrServerException {

        ResponseResult responseResult = ResponseResult.getResponseResult();
        client.deleteByQuery("*:*");
        client.commit();

        return responseResult;
    }

    /**
     * Excel文件上传 导入数据
     *
     * @param file
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping("/questionBatchImport")
    public ResponseResult questionBatchImport(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        ResponseResult responseResult = ResponseResult.getResponseResult();
        Map<String, Object> parameterMap = CommonUtils.getParameterMap(request);
//        questionBankService.readExcelData();
        //得到表格的输入流
        InputStream fis = file.getInputStream();
        Workbook workbook = null;
        if (file.getOriginalFilename().endsWith(".xls")) {//兼容 Excel 2003
            workbook = new HSSFWorkbook(fis);
        } else { //xlsx
            workbook = new XSSFWorkbook(fis);
        }
        Sheet sheet = workbook.getSheetAt(0);
        int physicalNumberOfRows = sheet.getPhysicalNumberOfRows();//获取数据的行数
        //获取第一行
        Row row1 = sheet.getRow(0);
        Cell cell = row1.getCell(0);
        //所有数据
        List<Map<String, Object>> listdata = new ArrayList<>();
        for (int i = 1; i < physicalNumberOfRows; i++) {
            HashMap<String, Object> maprow = new HashMap<>();
            Row row = sheet.getRow(i);
            maprow.put("tigan", row.getCell(0).getStringCellValue());
            maprow.put("xuanxiangbiaohao", row.getCell(1).getStringCellValue());
            ArrayList<String> list = new ArrayList<>();
            list.add(row.getCell(2).getStringCellValue());
            list.add(row.getCell(3).getStringCellValue());
            list.add(row.getCell(4).getStringCellValue());
            list.add(row.getCell(5).getStringCellValue());
            maprow.put("xuanxiangmiaoshu", JSON.toJSONString(list));
            maprow.put("daan", row.getCell(6).getStringCellValue());
            if (row.getCell(7) != null) {
                maprow.put("timujiexi", row.getCell(7).getStringCellValue());
            }
            listdata.add(maprow);
        }
        questionBankService.questionBatchImport(parameterMap,listdata);
        return responseResult;
    }

    @RequestMapping("")
    public ResponseResult xxx(){

        ResponseResult responseResult = ResponseResult.getResponseResult();




        return responseResult;
    }
}

