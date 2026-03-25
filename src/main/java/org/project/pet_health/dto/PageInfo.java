package org.project.pet_health.dto;

/*
分页查询
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageInfo<T> {
//    /*
//    页码
//     */
//    private Integer pageNumber;
//
//    /*
//    每页条数
//     */
//    private Integer pageSize;

    /*
    总条数
     */
    private Long total;

    /*
    数据列表
     */
    private List<T> list;
}
