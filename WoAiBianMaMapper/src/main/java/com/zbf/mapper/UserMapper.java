package com.zbf.mapper;

import com.zbf.core.page.Page;
import com.zbf.oauthLogin.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 作者：LCG
 * 创建时间：2019/1/23 18:02
 * 描述：
 *
 * @author chuck
 */
@Mapper
public interface UserMapper {

    /**
     * 根据登录名查找用户
     * @param loginnname
     * @return
     */
    User getUserByUserName(String loginnname);

    /**
     * 根据用户Id获取用户的信息
     * @param userid
     * @return
     */
    Map<String, Object> getUserById(String userid);

    /**
     * 查询用户的分页信息
     * @param page
     * @return
     */
    List<Map<String, Object>> getUserList(Page<Map<String, Object>> page);

    /**
     * 绑定用户角色
     * @param list
     * @return
     */
    int toBangDingRoleForUser(List<Map<String, Object>> list);

    /**
     * 删除指定userId的角色
     * @param userId
     * @return
     */
    int deleteRoleUser(String userId);

}
