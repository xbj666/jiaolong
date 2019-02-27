package com.zbf.entity;

import lombok.Data;
import org.apache.solr.client.solrj.beans.Field;

/**
 * @author chuck
 * @version 1.0
 * @date 2019/2/18 21:17
 */
@Data
public class Question {
    @Field
    private String id;
    @Field
    private String tikuname;
    @Field
    private String laiyuan;
    @Field
    private Long createuserid;
    @Field
    private Long nanduid;
    @Field
    private String tikuid;
    @Field
    private String jiexi;
    @Field
    private Long shitizhuangtai;
    @Field
    private Long tixingid;
    @Field
    private String tigan;
    @Field
    private String userName;
    @Field
    private String createtime;
    @Field
    private String[] questionlist;
}
