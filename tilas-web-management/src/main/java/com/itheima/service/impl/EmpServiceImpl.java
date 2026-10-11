package com.itheima.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.mapper.EmpExprMapper;
import com.itheima.mapper.EmpMapper;
import com.itheima.pojo.Emp;
import com.itheima.pojo.EmpExpr;
import com.itheima.pojo.EmpQueryParam;
import com.itheima.pojo.LoginInfo;
import com.itheima.pojo.PageResult;
import com.itheima.service.EmpService;
import com.itheima.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工业务实现。
 *
 * <p>员工与工作经历属于多表操作，统一使用
 * {@code @Transactional(rollbackFor = Exception.class)} 保证原子性。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmpServiceImpl implements EmpService {

    private final EmpMapper empMapper;
    private final EmpExprMapper empExprMapper;

    @Override
    public PageResult<Emp> page(EmpQueryParam empQueryParam) {
        // 1. 设置分页参数
        PageHelper.startPage(empQueryParam.getPage(), empQueryParam.getPageSize());
        // 2. 执行查询（PageHelper 自动改写 SQL 并返回 Page 对象）
        List<Emp> empList = empMapper.list(empQueryParam);
        Page<Emp> page = (Page<Emp>) empList;
        // 3. 封装分页结果
        return new PageResult<>(page.getTotal(), page.getResult());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(Emp emp) {
        // 1. 补全基础属性
        emp.setCreateTime(LocalDateTime.now());
        emp.setUpdateTime(LocalDateTime.now());
        // 2. 保存员工基本信息（自增主键回填到 emp.id）
        empMapper.insert(emp);
        // 3. 保存员工工作经历
        saveExprList(emp.getId(), emp.getExprList());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteByIds(List<Integer> ids) {
        // 1. 删除员工基本信息
        empMapper.deleteByIds(ids);
        // 2. 删除员工工作经历
        empExprMapper.deleteByEmpIds(ids);
    }

    @Override
    public Emp getInfo(Integer id) {
        return empMapper.getById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(Emp emp) {
        // 防御性校验：缺少主键的更新请求无法定位记录，提前给出明确提示
        if (emp.getId() == null) {
            throw new IllegalArgumentException("更新员工失败：员工 ID 不能为空");
        }
        // 1. 更新员工基本信息
        emp.setUpdateTime(LocalDateTime.now());
        empMapper.updateById(emp);
        // 2. 工作经历采用「先删后插」的全量覆盖策略
        empExprMapper.deleteByEmpIds(List.of(emp.getId()));
        // 3. 写入新的工作经历
        saveExprList(emp.getId(), emp.getExprList());
    }

    @Override
    public LoginInfo login(Emp emp) {
        Emp empLogin = empMapper.getUsernameAndPassword(emp);
        if (empLogin == null) {
            return null;
        }
        // 令牌负载中携带员工 id 与用户名，供拦截器解析
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", empLogin.getId());
        claims.put("username", empLogin.getUsername());
        String jwt = JwtUtils.generateJwt(claims);
        log.info("员工登录成功, id={}, username={}", empLogin.getId(), empLogin.getUsername());
        return new LoginInfo(empLogin.getId(), empLogin.getUsername(), empLogin.getName(), jwt);
    }

    /**
     * 批量保存工作经历，空集合直接跳过，避免生成非法 SQL。
     */
    private void saveExprList(Integer empId, List<EmpExpr> exprList) {
        if (CollectionUtils.isEmpty(exprList)) {
            return;
        }
        exprList.forEach(expr -> expr.setEmpId(empId));
        empExprMapper.insertBatch(exprList);
    }

}
