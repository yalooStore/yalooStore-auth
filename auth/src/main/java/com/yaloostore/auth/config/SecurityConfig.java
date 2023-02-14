package com.yaloostore.auth.config;


import com.yaloostore.auth.filter.JwtAuthenticationFilter;
import com.yaloostore.auth.jwt.JwtFailureHandler;
import com.yaloostore.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.data.redis.core.RedisTemplate;

import java.security.Security;


/**
 * spring boot security 관련 설정을 위한 클래스
 * */
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    private final RedisTemplate<String, Object> redisTemplate;


    /**
     * SecurityFilterChain을 사용해서 스프링이 설정해준 초기화 작업, 보안 설정을 개발자가 설정할 수 있게해준다.
     * */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http.authorizeHttpRequests().anyRequest().permitAll();
        http.formLogin().disable();
        http.logout().disable();
        http.csrf().disable();
        http.addFilter(jwtAuthenticationFilter());


        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.headers().frameOptions().sameOrigin();

        return http.build();
    }



    private JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter =new JwtAuthenticationFilter(authenticationManager(null),
                jwtTokenProvider, redisTemplate);


    }


    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


    /**
     * 인증을 관리하는 AuthenticationManager 반환하는 메소드
     * @param configuration 인증 구성을 매개변수로 넘겨준다.
     * @return 인증 정보를 관리하는 AuthenticationManager를 반환한다.
     * */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * 인증 실패시에 동작하는 핸들러
     * */

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler(){
        return new JwtFailureHandler();
    }

}
