package org.project.pet_health.dto;

/*
分页查询
 */

import lombok.Data;

@Data
public class PageInfo {
    /*
    页码
     */
    private Integer pageNumber;

    /*
    每页条数
     */
    private Integer pageSize;


}
