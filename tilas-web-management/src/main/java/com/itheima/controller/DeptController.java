package com.itheima.controller;

import com.itheima.anno.Log;
import com.itheima.pojo.Dept;
import com.itheima.pojo.Result;
import com.itheima.service.DeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 部门管理
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    /**
     * 查询全部部门
     */
    @GetMapping("/depts")
    public Result list() {
        log.info("查询全部部门数据");
        List<Dept> deptList = deptService.findAll();
        return Result.success(deptList);
    }

    /**
     * 根据 ID 删除部门
     */
    @Log
    @DeleteMapping("/depts")
    public Result delete(@RequestParam("id") Integer deptId) {
        log.info("删除部门, id={}", deptId);
        deptService.deleteById(deptId);
        return Result.success();
    }

    /**
     * 新增部门
     */
    @Log
    @PostMapping("/depts")
    public Result add(@RequestBody Dept dept) {
        log.info("新增部门, dept={}", dept);
        deptService.add(dept);
        return Result.success();
    }

    /**
     * 根据 ID 查询部门
     */
    @GetMapping("/depts/{id}")
    public Result getInfo(@PathVariable("id") Integer deptId) {
        log.info("根据 id 查询部门, id={}", deptId);
        Dept dept = deptService.getById(deptId);
        return Result.success(dept);
    }

    /**
     * 修改部门
     */
    @Log
    @PutMapping("/depts")
    public Result update(@RequestBody Dept dept) {
        log.info("修改部门, dept={}", dept);
        deptService.update(dept);
        return Result.success();
    }

}
