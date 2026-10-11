package com.itheima.mapper;

import com.itheima.pojo.Emp;
import com.itheima.pojo.EmpQueryParam;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 员工信息 Mapper。
 *
 * <p>复杂 SQL（动态条件、多表关联、批量操作）定义在
 * {@code resources/com/itheima/mapper/EmpMapper.xml} 中。</p>
 */
@Mapper
public interface EmpMapper {

    /**
     * 分页查询员工列表，动态条件见 XML。
     */
    List<Emp> list(EmpQueryParam empQueryParam);

    /**
     * 新增员工，并回填自增主键。
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into emp(username, name, gender, phone, job, salary, image, entry_date, dept_id, create_time, update_time) " +
            "values (#{username},#{name},#{gender},#{phone},#{job},#{salary},#{image},#{entryDate},#{deptId},#{createTime},#{updateTime})")
    void insert(Emp emp);

    /**
     * 根据 ID 批量删除员工。
     */
    void deleteByIds(@Param("ids") List<Integer> ids);

    /**
     * 根据 ID 查询员工详情（含工作经历列表）。
     */
    Emp getById(Integer id);

    /**
     * 根据 ID 动态更新员工信息。
     */
    void updateById(Emp emp);

    /**
     * 统计各职位员工人数。
     */
    List<Map<String, Object>> countEmpJobData();

    /**
     * 统计员工性别分布。
     */
    List<Map<String, Object>> countEmpGenderData();

    /**
     * 根据用户名与密码查询员工（登录校验用）。
     */
    @Select("select * from emp where username = #{username} and password = #{password}")
    Emp getUsernameAndPassword(Emp emp);

}
