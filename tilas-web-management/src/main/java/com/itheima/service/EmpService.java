package com.itheima.service;

import com.itheima.pojo.Emp;
import com.itheima.pojo.EmpQueryParam;
import com.itheima.pojo.LoginInfo;
import com.itheima.pojo.PageResult;

import java.util.List;

/**
 * 员工业务接口
 */
public interface EmpService {

    /**
     * 分页查询员工信息
     */
    PageResult<Emp> page(EmpQueryParam empQueryParam);

    /**
     * 新增员工及其工作经历
     */
    void save(Emp emp);

    /**
     * 根据 ID 批量删除员工及其工作经历
     */
    void deleteByIds(List<Integer> ids);

    /**
     * 根据 ID 查询员工详情
     */
    Emp getInfo(Integer id);

    /**
     * 修改员工信息及其工作经历
     */
    void update(Emp emp);

    /**
     * 员工登录，返回含 JWT 令牌的登录信息
     */
    LoginInfo login(Emp emp);

}
