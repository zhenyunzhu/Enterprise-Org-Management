package com.itheima.service;

import com.itheima.pojo.JobOption;

import java.util.List;
import java.util.Map;

/**
 * 数据统计业务接口
 */
public interface ReportService {

    /**
     * 统计各职位员工人数
     */
    JobOption getEmpJobData();

    /**
     * 统计员工性别分布
     */
    List<Map<String, Object>> getEmpGenderData();

}
