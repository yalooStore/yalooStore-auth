package com.yaloostore.auth.config;


import com.yaloostore.auth.filter.JwtAuthenticationFilter;
import com.yaloostore.auth.handler.JwtFailureHandler;
import com.yaloostore.auth.provider.JwtAuthenticationProvider;
import com.yaloostore.auth.service.impl.CustomUserDetailsService;
import com.yaloostore.auth.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtProvider jwtProvider;

    private final RedisTemplate<String, Object> redisTemplate;

    private final RestTemplate restTemplate;
    private final ServerMetaDataConfig serverMetaDataConfig;



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests().anyRequest().permitAll();
        http.formLogin().disable();
        http.logout().disable();
        http.csrf().disable();
        http.cors().disable();

        //jwt 발급관련 필터체인 등록
        http.addFilterAt(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.headers().frameOptions().sameOrigin();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return http.build();


    }



    private AbstractAuthenticationProcessingFilter jwtAuthenticationFilter() throws Exception {

        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager(null),
                jwtProvider, redisTemplate);
        jwtAuthenticationFilter.setAuthenticationFailureHandler(jwtFailureHandler());
        jwtAuthenticationFilter.setFilterProcessesUrl("/auth/login");
        //failureHandler 등록하기
        return jwtAuthenticationFilter;
    }

    @Bean
    public AuthenticationFailureHandler jwtFailureHandler() {
        return new JwtFailureHandler(restTemplate, serverMetaDataConfig);
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManager =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManager.authenticationProvider(jwtAuthenticationProvider());

        return authenticationManager.build();

    }

    @Bean
    public AuthenticationProvider jwtAuthenticationProvider() {
        return new JwtAuthenticationProvider(customUserDetailsService,
                bCryptPasswordEncoder());
    }


    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }


}
