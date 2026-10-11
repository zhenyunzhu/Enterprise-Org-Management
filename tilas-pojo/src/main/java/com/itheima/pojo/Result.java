package com.itheima.pojo;

import lombok.Data;

/**
 * 后端统一返回结果封装。
 *
 * <p>约定：{@code code = 1} 表示成功，{@code code = 0} 表示失败。
 * 当 {@code msg} 为 {@link #NOT_LOGIN} 时，前端据此跳转登录页。</p>
 */
@Data
public class Result {

    /** 未登录 / 令牌失效的约定错误码，前端拦截该值后重定向到登录页 */
    public static final String NOT_LOGIN = "NOT_LOGIN";

    /** 业务状态码：1 成功，0 失败 */
    private Integer code;

    /** 提示信息，失败时携带原因 */
    private String msg;

    /** 业务数据 */
    private Object data;

    public static Result success() {
        Result result = new Result();
        result.code = 1;
        result.msg = "success";
        return result;
    }

    public static Result success(Object object) {
        Result result = new Result();
        result.data = object;
        result.code = 1;
        result.msg = "success";
        return result;
    }

    public static Result error(String msg) {
        Result result = new Result();
        result.msg = msg;
        result.code = 0;
        return result;
    }

}
