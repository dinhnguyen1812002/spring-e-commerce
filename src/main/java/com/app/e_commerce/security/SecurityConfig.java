package com.app.e_commerce.security;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private DataSource dataSource;

    private String key="undersecretary";


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers( "/auth/register",
                                "/css/**",
                                "/js/**",
                                "/uploads/**",
                                "/images/**",
                                "/static/**")
                        .permitAll()
                        .requestMatchers("/products/{id}").permitAll()
                        .requestMatchers("/products").permitAll()
                        .requestMatchers("/products/**").authenticated()

                        .requestMatchers(HttpMethod.POST, "/products/create").authenticated()
                        .requestMatchers("/users/**").permitAll()
                        .requestMatchers("/user/**").authenticated()
                        .requestMatchers("/cart/**").authenticated()
                        .requestMatchers("/orders/**").authenticated()
                        .requestMatchers("/ratings/add").authenticated()
                        
                        .requestMatchers("/recommendations/popular").permitAll()
                        .requestMatchers("/recommendations").permitAll()
                        
                        // Invoice API endpoints
                        .requestMatchers("/api/invoices/**").authenticated()
                        .requestMatchers("/api/admin/invoice-templates/**").hasAnyAuthority("ADMIN", "INVOICE_MANAGER")
                        
                        // Invoice UI endpoints
                        .requestMatchers("/invoices/**").authenticated()
                        .requestMatchers("/admin/invoice-templates/**").hasAnyAuthority("ADMIN", "INVOICE_MANAGER")
                        
                        .requestMatchers("/admin/**").hasAnyAuthority("ADMIN")
                        .requestMatchers("/api/**").hasAnyAuthority("ADMIN")


                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .defaultSuccessUrl("/")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .rememberMe(rememberMe -> rememberMe
                        .rememberMeParameter("remember-me")
                        .tokenValiditySeconds(14*24*60*60)
                        .key(key)
                        .userDetailsService(userDetailsService)
                        .tokenRepository(persistentTokenRepository())
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        // Không cần tạo bảng thủ công, JPA sẽ tự động tạo dựa trên entity
        return tokenRepository;
    }
}