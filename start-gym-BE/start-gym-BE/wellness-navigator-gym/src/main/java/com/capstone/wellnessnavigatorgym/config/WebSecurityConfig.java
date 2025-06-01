package com.capstone.wellnessnavigatorgym.config;

import com.capstone.wellnessnavigatorgym.security.jwt.JwtEntryPoint;
import com.capstone.wellnessnavigatorgym.security.jwt.JwtFilter;
import com.capstone.wellnessnavigatorgym.security.userprinciple.UserDetailService;
import com.capstone.wellnessnavigatorgym.utils.BuildDecisionTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailService userDetailService;

    @Autowired
    private JwtEntryPoint jwtEntryPoint;

    @Autowired
    private JwtFilter jwtFilter;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailService);
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public BuildDecisionTree buildDecisionTree() {
        return new BuildDecisionTree();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf()
                .disable()
                .authorizeRequests()
                .antMatchers("/api/v1/public/**",
                        "/api/v1/course/**",
                        "/api/v1/course-day/**",
                        "/api/v1/course-type/**",
                        "/api/v1/comment/**",
                        "/api/v1/day/**",
                        "/api/v1/track-data-ai/**",
                        "/api/v1/payment/**",
                        "/api/v1/customer/list",
                        "/api/v1/cart/**",
                        "/api/v1/customer-course/debug-customer/**",
                        "/api/v1/customer-course/fix-vanba-account",
                        // Static resources - allow without authentication
                        "/uploads/**",
                        "/images/**",
                        "/static/**")
                .permitAll()
                // Các API cần authentication để thêm khóa học hoặc thao tác cá nhân
                .antMatchers("/api/v1/customer/detail",
                        "/api/v1/customer/register-course/**",
                        "/api/v1/customer/profile/**",
                        "/api/v1/customer/update/**",
                        "/api/v1/exercise-day/updateStatusForDay/**",
                        "/api/v1/customer-type/**",
                        "/api/v1/customer-course/**",
                        "/api/v1/users/info").hasAnyRole("USER", "ADMIN")
                // Admin only endpoints
                .antMatchers("/api/v1/customer/admin/**",
                        "/api/v1/admin/**",
                        "/api/v1/exercise/**").hasRole("ADMIN")
                .anyRequest()
                .authenticated()
                .and()
                .cors()
                .configurationSource(request -> {
                    CorsConfiguration corsConfiguration = new CorsConfiguration();
                    corsConfiguration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://127.0.0.1:3000"));
                    corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    corsConfiguration.setAllowedHeaders(Arrays.asList("*"));
                    corsConfiguration.setAllowCredentials(true);
                    return corsConfiguration;
                })
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(jwtEntryPoint)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
