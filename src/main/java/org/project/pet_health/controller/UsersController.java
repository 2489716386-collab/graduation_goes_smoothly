package org.project.pet_health.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.project.pet_health.common.Result;
import org.project.pet_health.dto.UserQuery;
import org.project.pet_health.entity.Users;
import org.project.pet_health.mapper.UsersMapper;
import org.project.pet_health.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author weiling
 * @since 2026-03-11
 */
@RestController
@RequestMapping("/users")
@Tag(name = "用户列表")
public class UsersController {

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private UsersService usersService;

    /*
    分页查询
     */

    @PostMapping("/page")
    public Result<?> findPage(@RequestBody UserQuery userQuery) {

        /*
        降序排列
         */
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Users::getUserId);

        /*
        模糊查询
         */
        if(!"".equals(userQuery.getNickname()) && userQuery.getNickname() != null){
            queryWrapper.like(Users::getNickname, userQuery.getNickname());
        }

        Page<Users> page = usersService.page(
                new Page<>(
                        userQuery.getPageNumber(),
                        userQuery.getPageSize()
                ),
                queryWrapper
        );
        return Result.success(page);
    }

    @GetMapping("/query")
    @Operation(summary = "查询")
     public List<Users> query() {
        System.out.println("----- 开始查询所有用户 -----");
        List<Users> userList = usersMapper.selectList(null);
        System.out.println("共查询到 " + userList.size() + " 条数据");
        userList.forEach(System.out::println);

        for (Users u : userList) {
            System.out.println("用户ID：" + u.getUserId());
            System.out.println("用户名：" + u.getUsername());
            System.out.println("昵称：" + u.getNickname());
        }
        return userList;
    }

    /*
    新增
     */
    @PostMapping("/save")
    public Result<?> save(@Validated @RequestBody Users users) {

        //throw new UserException("这个是自定义异常");
        usersService.saveOrUpdate(users);
        return Result.success();
    }

    /*
    批量删除
     */
    @PostMapping("/delete")
    public Result<?> delete(@RequestBody List<Integer> Ids) {
        usersService.removeByIds(Ids);
        return Result.success();
    }
}
