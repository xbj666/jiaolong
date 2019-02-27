package com.zbf.mapper;

import com.zbf.core.page.Page;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author chuck
 */
@Mapper
public interface RoleMapper {
    /**
     * 获取角色的列表
     * @param page
     * @return
     */
    List<Map<String,Object>> getRolePage(Page<Map<String, Object>> page);

    /**
     * 删除角色信息
     * @param map
     * @return
     */
    int deleteByRoleId(Map<String, Object> map);

    /**
     * 删除角色和菜单的绑定信息
     * @param map
     * @return
     */
    int deleteRoleMenu(Map<String, Object> map);

    /**
     * 添加角色
     * @param map
     * @return
     */
    int addRole(Map<String, Object> map);

    /**
     * 批量的插入角色菜单绑定数据
     * @param list
     * @return
     */
    int addRoleMenu(List<Map<String, Object>> list);

    /**
     * 更新角色
     * @param map
     * @return
     */
    int updateRole(Map<String, Object> map);

    /**
     * 通过查询条件获取角色列表
     * @param map
     * @return
     */
    List<Map<String,Object>> getRoleListByQuery(Map<String, Object> map);
}
