package org.project.pet_health.dto;

import lombok.Data;

/*
模糊查询
 */
@Data
public class UserQuery  extends PageInfo{
    private String nickname;
}
