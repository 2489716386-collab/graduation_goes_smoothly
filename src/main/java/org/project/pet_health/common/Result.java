package org.project.pet_health.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    @Schema(name = "状态码")
    private int code;

    @Schema(name = "提示")
    private String msg;

    @Schema(name = "数据")
    private Object data;

    public static Result success( ) {
        return new Result(200,"成功",null);
    }

    public static Result success(Object data ) {
        return new Result(200,"成功",data);
    }

    public static Result error( String msg ) {
        return new Result(500,msg,null);
    }

    public static Result error( int code, String msg ) {
        return new Result(code,msg,null);
    }

    public static Result error() {
        return new Result(500,"错误，请联系管理员！",null);
    }


}
