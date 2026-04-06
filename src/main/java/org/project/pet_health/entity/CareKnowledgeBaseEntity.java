package org.project.pet_health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * AI 智能养护知识基准表
 * </p>
 *
 * @author weiling
 * @since 2026-04-06
 */
@Getter
@Setter
@ToString
@TableName("care_knowledge_base")
@Tag(name = "CareKnowledgeBaseEntity对象", description = "AI 智能养护知识基准表")
public class CareKnowledgeBaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Schema(description ="主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 物种(如：猫、狗、异宠)
     */
    @TableField("species")
    @Schema(description ="物种(如：猫、狗、异宠)")
    private String species;

    /**
     * 品种(如：金毛、布偶，默认\"通用\")
     */
    @TableField("breed")
    @Schema(description ="品种(如：金毛、布偶，默认\"通用\")")
    private String breed;

    /**
     * 生命阶段(幼年/成年/老年/孕产期)
     */
    @TableField("life_stage")
    @Schema(description ="生命阶段(幼年/成年/老年/孕产期)")
    private String lifeStage;

    /**
     * 该阶段标准体重下限(kg)
     */
    @TableField("weight_min")
    @Schema(description ="该阶段标准体重下限(kg)")
    private BigDecimal weightMin;

    /**
     * 该阶段标准体重上限(kg)
     */
    @TableField("weight_max")
    @Schema(description ="该阶段标准体重上限(kg)")
    private BigDecimal weightMax;

    /**
     * 每公斤每日建议热量(kcal)
     */
    @TableField("kcal_per_kg")
    @Schema(description ="每公斤每日建议热量(kcal)")
    private Integer kcalPerKg;

    /**
     * 每日建议基础运动时长(分钟)
     */
    @Schema(description ="每日建议基础运动时长(分钟)")
    @TableField("exercise_mins_per_day")
    private Integer exerciseMinsPerDay;

    /**
     * 饮食规范(如：高蛋白、换粮过渡法)
     */
    @TableField("diet_advice")
    @Schema(description ="饮食规范(如：高蛋白、换粮过渡法)")
    private String dietAdvice;

    /**
     * 清洁美容规范(如：长毛需每日梳理，定期挤肛门腺)
     */
    @TableField("hygiene_advice")
    @Schema(description ="清洁美容规范(如：长毛需每日梳理，定期挤肛门腺)")
    private String hygieneAdvice;

    /**
     * 医疗预防规范(如：关注易发遗传病、按时驱虫)
     */
    @TableField("medical_advice")
    @Schema(description ="医疗预防规范(如：关注易发遗传病、按时驱虫)")
    private String medicalAdvice;

    /**
     * 环境心理规范(如：增加独处漏食玩具、猫砂盆数量)
     */
    @TableField("environment_advice")
    @Schema(description ="环境心理规范(如：增加独处漏食玩具、猫砂盆数量)")
    private String environmentAdvice;

    /**
     * 绝对禁忌(如：折耳猫禁止补钙、狗禁食葡萄巧克力)
     */
    @TableField("caution")
    @Schema(description ="绝对禁忌(如：折耳猫禁止补钙、狗禁食葡萄巧克力)")
    private String caution;

    /**
     * 该品种专属的AI提示词(如：你必须以兽医口吻着重强调该品种的心脏病风险)
     */
    @TableField("specific_prompt")
    @Schema(description ="该品种专属的AI提示词(如：你必须以兽医口吻着重强调该品种的心脏病风险)")
    private String specificPrompt;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
