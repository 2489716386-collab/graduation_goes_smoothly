package org.project.pet_health.common.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.project.pet_health.common.annotation.LogAction;
import org.project.pet_health.entity.AdminLogs;
import org.project.pet_health.service.AdminLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class AdminLogAspect {

    @Autowired
    private AdminLogsService adminLogsService;

    // 引入 SpEL 解析器
    private final SpelExpressionParser parser = new SpelExpressionParser();
    // 引入参数名发现器（用于获取方法参数的名字，如 "banDTO"）
    private final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    // 定义切点：所有被 @LogAction 注解标记的方法
    @Pointcut("@annotation(org.project.pet_health.common.annotation.LogAction)")
    public void logPointCut() {}

    // 只有在方法成功执行并返回后，才会保存日志
    @AfterReturning("logPointCut()")
    public void saveAdminLog(JoinPoint joinPoint) {
        try {
            // 1. 获取 Request 对象
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return;
            HttpServletRequest request = attributes.getRequest();

            // 2. 从你配置的 LoginInterceptor 中获取当前操作的管理员 ID
            Long adminId = (Long) request.getAttribute("currentUserId");

            // 3. 获取操作人的 IP 地址
            String ipAddress = request.getRemoteAddr();

            // 4. 获取注解上填写的 action 描述 (例如："封禁了用户")
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            LogAction logAction = method.getAnnotation(LogAction.class);
            String actionDesc = logAction != null ? logAction.value() : "执行了未知操作";

            // 如果注解内容包含 #{ ，说明有动态参数需要解析
            if (actionDesc.contains("#{")) {
                Object[] args = joinPoint.getArgs(); // 获取实际传入的参数值
                String[] parameterNames = discoverer.getParameterNames(method); // 获取参数名
                EvaluationContext context = new StandardEvaluationContext();

                // 将方法参数绑定到 SpEL 的上下文中
                if (parameterNames != null) {
                    for (int i = 0; i < parameterNames.length; i++) {
                        context.setVariable(parameterNames[i], args[i]);
                    }
                }
                try {
                    // 使用模板解析器替换占位符
                    Expression expression = parser.parseExpression(actionDesc, new TemplateParserContext());
                    actionDesc = expression.getValue(context, String.class);
                } catch (Exception e) {
                    log.error("AOP日志SpEL解析失败: {}", actionDesc, e);
                }
            }

            // 5. 组装实体并异步保存到数据库
            AdminLogs logEntity = new AdminLogs();
            logEntity.setAdminId(adminId);
            logEntity.setAction(actionDesc);
            logEntity.setIpAddress(ipAddress);

            // 保存日志
            adminLogsService.save(logEntity);
            log.info("AOP拦截成功！当前获取到的管理员ID为: {}", adminId);

        } catch (Exception e) {
            log.error("自动记录管理员操作日志失败: {}", e.getMessage());
        }
    }
}
