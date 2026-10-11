package com.itheima.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 报表通用封装：{@code jobList} 为分类轴，{@code dataList} 为对应数值。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobOption {

    /** 分类轴数据，如职位名称 */
    private List<Object> jobList;

    /** 数值轴数据，如职位人数 */
    private List<Object> dataList;

}
