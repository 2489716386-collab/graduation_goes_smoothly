package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.project.pet_health.dto.CarePlansDayProgressDTO;
import org.project.pet_health.entity.CarePlansDayEntity;
import org.project.pet_health.exception.UserException;
import org.project.pet_health.mapper.CarePlansDayMapper;
import org.project.pet_health.service.CarePlansDayService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 每日养护打卡任务表 服务实现类
 * </p>
 *
 * @author weiling
 * @since 2026-04-06
 */
@Service
@Slf4j
public class CarePlansDayServiceImpl extends ServiceImpl<CarePlansDayMapper, CarePlansDayEntity> implements CarePlansDayService {

    @Override
    public CarePlansDayProgressDTO getTodayPlanProgress(Long petId) {
        // 1. 获取系统今天的日期
        LocalDate today = LocalDate.now();

        // 2. 从数据库中查询该宠物今天的全部任务
        List<CarePlansDayEntity> tasks = this.list(new LambdaQueryWrapper<CarePlansDayEntity>()
                .eq(CarePlansDayEntity::getPetId, petId)
                .eq(CarePlansDayEntity::getPlanDate, today) // 仅限今天
                .orderByAsc(CarePlansDayEntity::getId));

        // 3. 计算完成进度百分比 (0 ~ 100)
        int progress = 0;
        if (tasks != null && !tasks.isEmpty()) {
            long completedCount = tasks.stream()
                    .filter(t -> t.getIsCompleted() != null && t.getIsCompleted() == 1) // 统计已完成的(isCompleted = 1)
                    .count();
            progress = (int) ((completedCount * 100) / tasks.size());
        }

        // 4. 封装成 DTO 返回给前端
        CarePlansDayProgressDTO dto = new CarePlansDayProgressDTO();
        dto.setTasks(tasks);
        dto.setProgress(progress);

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int toggleCheckIn(Long taskId) {
        CarePlansDayEntity task = this.getById(taskId);
        if (task == null) {
            throw new UserException("任务不存在");
        }

        // 切换打卡状态 (0 -> 1 或 1 -> 0)
        task.setIsCompleted(task.getIsCompleted() == 1 ? 0 : 1);
        this.updateById(task);

        // 重新计算该宠物今日的总进度并返回
        List<CarePlansDayEntity> todayTasks = this.list(new LambdaQueryWrapper<CarePlansDayEntity>()
                .eq(CarePlansDayEntity::getPetId, task.getPetId())
                .eq(CarePlansDayEntity::getPlanDate, LocalDate.now()));

        return calculateProgress(todayTasks);
    }

    // 内部私有方法：计算百分比逻辑
    private int calculateProgress(List<CarePlansDayEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) return 0;
        long completedCount = tasks.stream()
                .filter(t -> t.getIsCompleted() == 1)
                .count();
        return (int) ((completedCount * 100.0) / tasks.size());
    }
}
