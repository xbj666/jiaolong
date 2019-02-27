package com.zbf.core.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
/**
 * @author chuck
 * @version 1.0
 * @date 2019/2/18 19:34
 */
public class SolrUtil {
    private HttpSolrClient client = null;

    public SolrUtil(String url)
    {
        //SolrJ-7.3.1版本连接Solr
        client = new HttpSolrClient.Builder(url).build();
        //SolrJ-5.5.4版本连接Solr
        //client = new HttpSolrClient(url);
    }

    /**
     * 将Map对象添加到Solr中
     * @param m
     */
    public boolean addMap(Map m)
    {
        //Key-Value,
        //Key Set;Value Collection
        Set kset = m.keySet();
        SolrInputDocument doc = new SolrInputDocument();
        for(Object k:kset)
        {
            Object val = m.get(k);
            if(val != null)
                doc.addField(k.toString(), val);
        }
        try
        {
            client.add(doc);
            UpdateResponse resp = client.commit();
            if(resp.getStatus() == 0)
                return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新Solr中的文档，Map对象中必须存在id键用于定位doc文档
     * Map中其他的键值对是修改的内容，Key<String>代表数据域名称,
     * Value<Object>代表修改值
     * @param map
     */
    public boolean update(Map<String,Object> map)
    {
        try
        {
            String id = (String)map.get("id");
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField("id", id);
            for(String k:map.keySet())
            {
                //数据域Id忽略更新
                if(!"id".equals(k))
                {
                    Map map2 = new HashMap();
                    map2.put("set", map.get(k));
                    doc.addField(k, map2);
                }
            }
            client.add(doc);
            UpdateResponse resp = client.commit();
            if(resp.getStatus() == 0)
                return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 通过文档ID删除Solr中的文档
     * @param id
     */
    public boolean deleteById(String id)
    {
        try
        {
            client.deleteById(id);
            UpdateResponse resp = client.commit();
            if(resp.getStatus() == 0)
                return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 通过查询删除Solr中对应的数据集合
     * @param query
     */
    public boolean deleteByQuery(String query)
    {
        try
        {
            client.deleteByQuery(query);
            UpdateResponse resp = client.commit();
            if(resp.getStatus() == 0)
                return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 通过泛型获取Solr中的对象集合
     * @param clz 泛型类对应java.lang.Class
     * @param query 数据域名称:数据域的值；查询全部*:*；多条件查询 name:Java AND age:20
     * @param flList 高亮显示数据域名称，是List<String>集合
     * @param page 分页查询时，开始记录数
     * @param rows 本次查询检索记录数
     * @return
     */
    public <T> ResultInfo<T> queryHL(Class<T> clz,String query,List<String> flList,
                                     Integer page,Integer rows)
    {
        try
        {
            //定义返回自定义数据结构对象
            ResultInfo<T> rslt = new ResultInfo<T>();
            SolrQuery q = new SolrQuery();
            q.set("q", query);
            q.set("fl","*");
            q.setHighlight(true);
            //高亮显示字段
            String hlField = "";
            for(String s:flList)
                hlField = hlField + s + ",";
            if(hlField.endsWith(","))
                hlField = hlField.substring(0,hlField.length()-1);
            q.set("hl.fl",hlField);
            //
            q.setHighlightSimplePre("<font color=\"red\">");
            q.setHighlightSimplePost("</font>");
            q.setStart(page);
            q.setRows(rows);
            QueryResponse qr = client.query(q);
            Map<String,Map<String,List<String>>> hlMap = qr.getHighlighting();
            System.out.println(hlMap);
            //Map<ID,Map<FieldName,[MultiValue]>>
            SolrDocumentList lst = qr.getResults();
            List<T> rtn = new ArrayList<T>();
            Long total = qr.getResults().getNumFound();

            for(SolrDocument doc:lst)
            {
                String id = (String)doc.getFieldValue("id");
                T t = clz.newInstance();
                //获取自定义类所有属性名称
                Field[] flds = getField(clz);
                for(Field field:flds)
                {
                    String fname = field.getName();
                    String solrFldName = getSolrFieldName(clz,field);
                    String fObj = getSingleValue(doc.getFieldValue(solrFldName));
                    if(fObj == null)
                        continue;
                    if(field.getType() == java.sql.Date.class)
                    {
                        java.util.Date dt = new java.util.Date(fObj);
                        fObj = new java.sql.Date(dt.getTime()).toString();
                    }
                    if(field.getType() == java.sql.Timestamp.class)
                    {
                        java.util.Date dt = new java.util.Date(fObj);
                        fObj = new java.sql.Timestamp(dt.getTime()).toString();
                    }
                    if(field.getType() == java.sql.Time.class)
                    {
                        java.util.Date dt = new java.util.Date(fObj);
                        fObj = new java.sql.Time(dt.getTime()).toString();
                    }
                    //高亮显示数据形式
                    ////Map<ID,Map<FieldName,[MultiValue]>>
                    if(flList.contains(fname))
                    {
                        //Map<FieldName,List<MultiValue>>
                        Map<String,List<String>> fldMap = hlMap.get(id);
                        Object hlObj = fldMap.get(fname);
                        String hlVal = getSingleValue(hlObj);
                        if(hlVal != null)
                            fObj = hlVal;

                    }
                    if(fObj != null)
                        BeanUtils.setProperty(t, fname, fObj);
                }
                rtn.add(t);
            }
            rslt.setList(rtn);
            rslt.setTotal(total);
            return rslt;

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 转化多值域为单值
     * @param obj
     * @return
     */
    private String getSingleValue(Object obj)
    {
        if(obj == null)
            return null;
        String val = obj.toString();
        if(val.startsWith("[") && val.endsWith("]"))
        {
            return val.substring(1,val.length()-1);
        }
        return val;
    }

    /**
     * 根据Class对象获取此类型的定义属性数据
     * @param clz
     * @return
     */
    private Field[] getField(Class clz)
    {
        Field[] flds = clz.getDeclaredFields();
        return flds;
    }

    /**
     * 通过Field对象取得其上定义的注解名称
     * @param clz
     * @param fld
     * @return
     */
    private String getSolrFieldName(Class clz,Field fld)
    {
        org.apache.solr.client.solrj.beans.Field fld2 =
                fld.getAnnotation(org.apache.solr.client.solrj.beans.Field.class);
        if(fld2 == null)
            return fld.getName();

        if(fld2.value().equals("#default"))
            return fld.getName();
        else
            return fld2.value();

    }

    public static void main(String[] args)
    {
        System.out.println(new java.util.Date());
        Map m = new HashMap();
        m.put("sid", "10");
        m.put("name","Java");
        String url = "http://localhost:8984/solr/new_core2/";
        SolrUtil solr = new SolrUtil(url);
        solr.addMap(m);

    }
}