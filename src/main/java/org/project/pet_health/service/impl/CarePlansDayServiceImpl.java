package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.entity.CarePlansDayEntity;
import org.project.pet_health.exception.UserException;
import org.project.pet_health.mapper.CarePlansDayMapper;
import org.project.pet_health.service.CarePlansDayService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CarePlansDayServiceImpl extends ServiceImpl<CarePlansDayMapper, CarePlansDayEntity> implements CarePlansDayService {

    @Override
    public List<CarePlansDayEntity> getTodayTasks(Long petId) {
        // 直接查询该宠物今天的任务列表
        return this.list(new LambdaQueryWrapper<CarePlansDayEntity>()
                .eq(CarePlansDayEntity::getPetId, petId)
                .eq(CarePlansDayEntity::getPlanDate, LocalDate.now())
                .orderByAsc(CarePlansDayEntity::getId));
    }

    @Override
    public boolean checkInTask(Long taskId) {
        // 1. 查出任务
        CarePlansDayEntity task = this.getById(taskId);
        if (task == null) {
            throw new UserException("打卡失败：未找到该任务");
        }

        // 2. 防重复打卡校验
        if (task.getIsCompleted() != null && task.getIsCompleted() == 1) {
            throw new UserException("该任务已经打过卡啦！");
        }

        // 3. 修改状态为 1，并记录当前精确时间
        task.setIsCompleted(1);
        task.setCompleteTime(LocalDateTime.now());

        // 4. 更新到数据库
        return this.updateById(task);
    }
}
