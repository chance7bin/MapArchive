package com.opengms.maparchivebackendprj.config;

import com.opengms.maparchivebackendprj.interceptor.AuthenticationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Description web配置
 * @Author bin
 * @Date 2021/10/10
 */
@Configuration
public class WebConfig implements WebMvcConfigurer{

    //解决跨域问题
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**"); //允许远端访问的域名
            // .allowedOrigins("http://localhost:8099")
            //允许请求的方法("POST", "GET", "PUT", "OPTIONS", "DELETE")
            // .allowedMethods("*")
            //允许请求头
            // .allowedHeaders("*");
    }

    //登录请求问题
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        /**
         * kn4j 在想文档相关的资源  需要放开
         */
        String[]  swaggerPatterns =  {
                "/swagger-resources/**",
                "/webjars/**",
                "/v2/**",
                "/swagger-ui.html/**",
                "/doc.html/**" };
        registry.addInterceptor(authenticationInterceptor())
                .excludePathPatterns(swaggerPatterns)
                .addPathPatterns("/**");    // 拦截所有请求，通过判断是否有 @LoginRequired 注解 决定是否需要登录
    }
    @Bean
    public AuthenticationInterceptor authenticationInterceptor() {
        return new AuthenticationInterceptor();
    }
}
