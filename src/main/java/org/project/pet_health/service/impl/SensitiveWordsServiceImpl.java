package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.project.pet_health.entity.SensitiveWords;
import org.project.pet_health.mapper.SensitiveWordsMapper;
import org.project.pet_health.service.SensitiveWordsService;
import org.project.pet_health.utils.SensitiveWordFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SensitiveWordsServiceImpl extends ServiceImpl<SensitiveWordsMapper, SensitiveWords> implements SensitiveWordsService {

    @Resource
    private SensitiveWordFilter sensitiveWordFilter;

    // --- 1. DFA 内存树初始化/刷新逻辑 ---
    @Override
    @PostConstruct
    public void refreshDFA() {
        List<SensitiveWords> list = this.list();
        // 注意：这里改成了 getWordContent，跟你实体类保持完全一致！
        Set<String> set = list.stream()
                .map(SensitiveWords::getWordContent)
                .collect(Collectors.toSet());
        sensitiveWordFilter.initSensitiveWordMap(set);
        System.out.println("🔄 DFA 敏感词库已同步更新，当前词条数：" + set.size());
    }

    // --- 2. 你原有的代码保留：后台分页查询 ---
    @Override
    public Page<SensitiveWords> getAdminPage(Integer pageNum, Integer pageSize, String word) {
        LambdaQueryWrapper<SensitiveWords> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(word)) {
            wrapper.like(SensitiveWords::getWordContent, word);
        }
        wrapper.orderByDesc(SensitiveWords::getId);
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    // --- 3. 你原有的代码保留：批量删除，并加上了刷新逻辑 ---
    @Override
    public void batchDeleteWords(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            this.removeByIds(ids); // MyBatis-Plus 自带的批量删除
            this.refreshDFA();     // 【新增】：删除完成后，刷新 DFA 内存树
        }
    }

    // --- 4. 【核心重写】：重写父类的方法，挂载 DFA 刷新操作 ---

    @Override
    public boolean save(SensitiveWords entity) {
        boolean result = super.save(entity); // 先让父类去执行数据库的保存
        if (result) {
            this.refreshDFA(); // 保存成功后，刷新咱们的内存字典
        }
        return result;
    }

    @Override
    public boolean updateById(SensitiveWords entity) {
        boolean result = super.updateById(entity); // 先让父类去执行数据库的修改
        if (result) {
            this.refreshDFA(); // 修改成功后，刷新内存字典
        }
        return result;
    }

    @Override
    public boolean removeById(Serializable id) {
        boolean result = super.removeById(id); // 先让父类去执行数据库的删除
        if (result) {
            this.refreshDFA(); // 删除成功后，刷新内存字典
        }
        return result;
    }
}
