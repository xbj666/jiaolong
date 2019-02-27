package com.zbf.mapper;

import com.zbf.core.page.Page;
import com.zbf.entity.Menu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 作者：LCG
 * 创建时间：2018/11/23 15:52
 * 描述：
 * @author chuck
 */
@Mapper
public interface MenuMapper {
    /**
     * 获取菜单列表
     * @param menu
     * @return
     */
    List<Menu> getListMenu(Menu menu);

    /**
     * 通过角色id获取菜单列表
     * @param menu
     * @return
     */
    List<Menu> getListMenuByRoleIds(Menu menu);

    /**
     * 获取分页菜单列表
     * @param page
     * @return
     */
    List<Menu> menuList(Page<Menu> page);

    /**
     * 添加次级菜单
     * @param menu
     */
    void addMenu(Menu menu);

    /**
     * 添加一级菜单
     * @param menu
     */
    void addFirstMenu(Menu menu);

    /**
     * 更新菜单
     * @param menu
     */
    void updateMenu(Menu menu);

    /**
     * 删除菜单(过时)
     * @param menu
     */
    void deleteMenu(Menu menu);

    /**
     * 通过id查找子菜单
     * @param id
     * @return
     */
    List<Long> findChildrenMenuById(Long id);

    /**
     * 设置所有要删除的记录的isDelete字段为1
     * @param list
     */
    void updateMenuIsDelete(List<Long> list);

    /**
     * 删除所有isDelete字段为1的记录
     */
    void deleteAll();

}
