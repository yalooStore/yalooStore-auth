package com.yaloostore.auth.config;


import com.yaloostore.auth.config.filter.JwtAuthenticationFilter;
import jakarta.servlet.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.formLogin().disable();
        http.csrf().disable();
        http.cors().disable();


        //jwt 발급관련 필터체인 등록

        http.addFilter(jwtAuthenticationFilter());

        return http.build();


    }



    private AbstractAuthenticationProcessingFilter jwtAuthenticationFilter() {

        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager());

    }

    public AuthenticationManager authenticationManager(){



    }


}
