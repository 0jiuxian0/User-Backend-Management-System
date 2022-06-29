package com.jiuxian.springboot.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.jiuxian.springboot.entity.User;
import com.jiuxian.springboot.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Component
public class TokenUtils{
    private static IUserService staticUserService;
    @Autowired
    private IUserService userService;
    @PostConstruct
    public void setUserService(){
        staticUserService = userService;
    }

    /**
     * 生成token
     * @return
     */
    public static String genToken(String userId,String sign){
        return JWT.create().withAudience(userId) //将user id保存到token里面，作为在和
                .withExpiresAt(DateUtil.offsetHour(new Date(),2)) //2小时后token过期
                .sign(Algorithm.HMAC256(sign)); //以password作为token的密钥
    }

    /**
     * 获取当前登录的用户信息
     * @return
     */
    public static User getCurrentUser(){
        try{
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = request.getHeader("token");
            if (StrUtil.isNotBlank(token)) {
                String userId = JWT.decode(token).getAudience().get(0);
                return staticUserService.getById(Integer.valueOf(userId));
            }
        }catch (Exception e){
            return  null;
        }
        return null;
    }
}
