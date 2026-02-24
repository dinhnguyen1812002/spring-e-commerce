package com.app.e_commerce.security;

import java.util.List;

import javax.sql.DataSource;

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
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final DataSource dataSource;

    private static final String REMEMBER_ME_KEY = "undersecretary";

    public SecurityConfig(UserDetailsService userDetailsService,
                          DataSource dataSource) {
        this.userDetailsService = userDetailsService;
        this.dataSource = dataSource;
    }

    @Bean
    public RememberMeServices rememberMeServices(PersistentTokenRepository tokenRepository) {
        PersistentTokenBasedRememberMeServices services = new PersistentTokenBasedRememberMeServices(
                REMEMBER_ME_KEY, userDetailsService, tokenRepository) {
            @Override
            public org.springframework.security.core.Authentication autoLogin(
                    jakarta.servlet.http.HttpServletRequest request,
                    jakarta.servlet.http.HttpServletResponse response) {
                try {
                    return super.autoLogin(request, response);
                } catch (org.springframework.security.web.authentication.rememberme.CookieTheftException ex) {
                    // Mark a short-lived cookie so UI can show a friendly message
                    jakarta.servlet.http.Cookie warn = new jakarta.servlet.http.Cookie("rm_err", "theft");
                    warn.setPath("/");
                    warn.setHttpOnly(true);
                    warn.setMaxAge(60);
                    response.addCookie(warn);
                    throw ex; // rethrow to keep default security behavior
                }
            }
        };
        services.setTokenValiditySeconds(14 * 24 * 60 * 60);
        services.setParameter("remember-me");
        return services;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, RememberMeServices rememberMeServices) throws Exception {

        http
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/auth/register",
                                "/css/**",
                                "/js/**",
                                "/uploads/**",
                                "/images/**",
                                "/static/**")
                        .permitAll()
                        .requestMatchers("/products", "/products/{id}").permitAll()
                        .requestMatchers("/products/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/products/create").authenticated()
                        .requestMatchers("/users/**").permitAll()
                        .requestMatchers("/user/**",
                                "/cart/**",
                                "/orders/**",
                                "/ratings/add").authenticated()
                        .requestMatchers("/recommendations",
                                "/recommendations/popular").permitAll()
                        .requestMatchers("/api/invoices/**").authenticated()
                        .requestMatchers("/api/admin/invoice-templates/**")
                        .hasAnyAuthority("ADMIN", "INVOICE_MANAGER")
                        .requestMatchers("/invoices/**").authenticated()
                        .requestMatchers("/admin/invoice-templates/**")
                        .hasAnyAuthority("ADMIN", "INVOICE_MANAGER")
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/**").hasAuthority("ADMIN")
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .rememberMeServices(rememberMeServices)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
    }

}
