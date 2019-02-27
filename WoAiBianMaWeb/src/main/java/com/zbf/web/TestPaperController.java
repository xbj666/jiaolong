package com.zbf.web;

import com.zbf.common.ResponseResult;
import com.zbf.core.CommonUtils;
import com.zbf.core.page.Page;
import com.zbf.service.TestPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 1.0
 * @date 2019/2/21 21:08
 */
@RestController
@RequestMapping("testpaper")
public class TestPaperController {

    @Autowired
    private TestPaperService testPaperService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加试卷分类
     *
     * @param request
     * @return
     */
    @RequestMapping("/addFenlei")
    public ResponseResult addFenlei(HttpServletRequest request) {

        Map<String, Object> parameterMap = CommonUtils.getParamsJsonMap(request);
        ResponseResult responseResult = ResponseResult.getResponseResult();
        //往mysql和redis中添加
        try {
            testPaperService.addFenlei(parameterMap, redisTemplate);
            responseResult.setSuccess("ok");
        } catch (Exception e) {
            e.printStackTrace();
            responseResult.setError("error");
        }

        return responseResult;
    }

    /**
     * 试卷分类列表
     *
     * @param request
     * @return
     */
    @RequestMapping("/fenleiList")
    public ResponseResult fenleiList(HttpServletRequest request) {
        Map<String, Object> parameterMap = CommonUtils.getParamsJsonMap(request);
        ResponseResult responseResult = ResponseResult.getResponseResult();
        Page<Map<String, Object>> page = new Page<>();
        Page.setPageInfo(page, parameterMap);
        //查询Redis中的 分类数据
        try {
            testPaperService.fenleiList(page, redisTemplate);
            responseResult.setResult(page);
            responseResult.setSuccess("ok");
        } catch (Exception e) {
            e.printStackTrace();
            responseResult.setError("error");
        }

        return responseResult;
    }

    /***
     *
     * user信息
     *
     */
    @RequestMapping("/userInfo")
    public ResponseResult userInfo(HttpServletRequest request){

        ResponseResult responseResult=ResponseResult.getResponseResult();

        Map<String, Object> parameterMap = CommonUtils.getParameterMap(request);

        Page<Map<String, Object>> page = new Page<>();

        Page.setPageInfo(page, parameterMap);

        testPaperService.userInfo(page);

        responseResult.setResult(page);

        return responseResult;
    }



    /**
     * 获取试卷分类
     *
     * @return
     */
    @RequestMapping("/getFenlei")
    public ResponseResult getFenlei() {
        ResponseResult responseResult = ResponseResult.getResponseResult();
        try {
            List<Map<String, Object>> fenlei = testPaperService.getFenlei(redisTemplate);
            responseResult.setResult(fenlei);
            responseResult.setSuccess("ok");
        } catch (Exception e) {
            e.printStackTrace();
            responseResult.setError("error");
        }
        return responseResult;
    }

    //添加试卷
    @RequestMapping("/addshijuan")
    public ResponseResult addshijuan(HttpServletRequest request){
        
        ResponseResult responseResult=ResponseResult.getResponseResult();

        Map<String, Object> parameterMap = CommonUtils.getParameterMap(request);

        testPaperService.addshijuan(parameterMap);


        return responseResult;
    }
}
