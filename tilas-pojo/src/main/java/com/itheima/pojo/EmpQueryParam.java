package com.itheima.pojo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 员工分页查询条件。
 *
 * <p>{@code page} 与 {@code pageSize} 给出默认值，避免前端未传参时
 * PageHelper 收到 null 抛出异常。</p>
 */
@Data
public class EmpQueryParam {

    /** 页码，从 1 开始 */
    private Integer page = 1;

    /** 每页记录数 */
    private Integer pageSize = 10;

    /** 入职日期区间 - 开始 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate begin;

    /** 入职日期区间 - 结束 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate end;

    /** 姓名，模糊匹配 */
    private String name;

    /** 性别，1 男 2 女 */
    private String gender;

}
