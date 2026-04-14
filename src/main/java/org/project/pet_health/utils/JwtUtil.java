package org.project.pet_health.utils;

import com.github.xiaoymin.knife4j.core.util.StrUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.project.pet_health.entity.Users;
import org.project.pet_health.service.UsersService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类（适配 jjwt 0.9.1 版本）
 */
@Component
public class JwtUtil {

    @Resource
    private UsersService usersService;

    // Token 过期时间：30天（单位：毫秒）
    public static final long TOKEN_EXPIRED_TIME = 30L * 24 * 60 * 60 * 1000;
    // JWT ID（唯一标识）
    public static final String jwtId = "tokenId";
    // JWT 加密密钥（建议自定义复杂字符串）
    private static final String JWT_SECRET = "PetHealth@2026#SecretKey123456";

    // ==================== 核心方法 ====================

    /**
     * 由字符串生成加密 SecretKey（HS256 算法）
     */
    public static SecretKey generalKey() {
        byte[] encodedKey = Base64.getEncoder().encode(JWT_SECRET.getBytes());
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, "HmacSHA256");
    }

    /**
     * 创建 JWT Token
     * @param claims 自定义载荷（存放用户信息）
     * @param time 过期时间（毫秒）
     * @return 生成的 Token
     */
    public static String createJWT(Map<String, Object> claims, Long time) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        SecretKey secretKey = generalKey();
        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .setId(jwtId)
                .setIssuedAt(now)
                .signWith(signatureAlgorithm, secretKey);

        // 如果设置了过期时间
        if (time != null && time >= 0) {
            long expMillis = nowMillis + time;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }

        return builder.compact();
    }

    /**
     * 验证 JWT Token，返回载荷
     * @param token 待验证的 Token
     * @return 载荷 Claims，验证失败返回 null
     */
    public static Claims verifyJwt(String token) {
        SecretKey key = generalKey();
        try {
            return Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据 User 实体生成 Token
     * @param users 用户实体
     * @return 生成的 Token
     */
    public static String generateToken(Users users) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", users.getUserId());
        map.put("username", users.getUsername());
        map.put("role", users.getRole()); // 把角色存入 token
        // 如果需要 openId，可以加参数传入
        // map.put("openId", openId);
        // map.put("sub", openId);
        return createJWT(map, TOKEN_EXPIRED_TIME);
    }

    /**
     * 获取当前登录用户详细信息（从请求头 Token 中解析）
     * @return User 对象，未登录返回 null
     */
    public Users getCurrentUserInfo() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return null;

            HttpServletRequest request = attributes.getRequest();
            String token = request.getHeader("token");

            if (StrUtil.isNotBlank(token)) {
                Claims claims = verifyJwt(token);
                if (claims != null) {
                    // 注意：你之前存的是 userId，这里要对应取值
                    Object userIdObj = claims.get("userId");
                    if (userIdObj != null) {
                        Long userId = Long.valueOf(userIdObj.toString());
                        return usersService.getById(userId);
                    }
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
