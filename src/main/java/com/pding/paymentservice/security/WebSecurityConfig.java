package com.pding.paymentservice.security;

import com.pding.paymentservice.security.jwt.AuthEntryPointJwt;
import com.pding.paymentservice.security.jwt.AuthTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
//@EnableWebSecurity
@EnableMethodSecurity
        (//securedEnabled = true,
                //      jsr250Enabled = true,
                prePostEnabled = true)
public class WebSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers(HttpMethod.OPTIONS).permitAll()
                                .requestMatchers("/api/payment/charge").authenticated()
                                .requestMatchers("/api/payment/test").authenticated()
                                .requestMatchers("/api/payment/wallet").authenticated()
                                .requestMatchers("/api/payment/wallethistory").authenticated()
                                .requestMatchers("/api/payment/buyvideo").authenticated()
                                .requestMatchers("/api/payment/videotransactions").authenticated()
                                .requestMatchers("/api/payment/treesEarned").authenticated()
                                .requestMatchers("/api/payment/isVideoPurchased").authenticated()
                );


        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}