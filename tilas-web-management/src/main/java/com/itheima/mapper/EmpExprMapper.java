package com.itheima.mapper;

import com.itheima.pojo.EmpExpr;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 员工工作经历 Mapper。
 *
 * <p>参数统一使用 {@code @Param} 显式命名，与 XML 中的
 * {@code collection} 属性严格对应，避免依赖编译期参数名导致绑定失败。</p>
 */
@Mapper
public interface EmpExprMapper {

    /**
     * 批量新增工作经历。
     */
    void insertBatch(@Param("list") List<EmpExpr> exprList);

    /**
     * 根据员工 ID 批量删除工作经历。
     */
    void deleteByEmpIds(@Param("empIds") List<Integer> empIds);

}
