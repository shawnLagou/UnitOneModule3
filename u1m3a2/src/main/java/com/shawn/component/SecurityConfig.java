package com.shawn.component;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author ：Shawn
 * @date ：Created in 2020/7/26 21:48
 * @description：
 * @modified By：
 * @version: $
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/css/**", "/index").permitAll()
                .antMatchers("/user/**").hasRole("USER")
                .and()
                .formLogin()
                .and()
                .csrf().disable() //关闭CSRF
                .formLogin().loginPage("/login")
                .loginProcessingUrl("/form")
                .defaultSuccessUrl("/index") //成功登陆后跳转页面
                .failureUrl("/loginError").permitAll();
    }

}
