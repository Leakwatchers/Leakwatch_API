package com.unifor.br.leakwatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // Habilita a segurança web
public class SecurityConfig {

    // Configura o PasswordEncoder (já fizemos isso)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configura as regras de segurança para as requisições HTTP
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // 1. Desabilita o CSRF (Cross-Site Request Forgery)
                // É comum em APIs REST que usam tokens em vez de sessões
                .csrf(csrf -> csrf.disable())

                // 2. Configura as autorizações de requisição
                .authorizeHttpRequests(auth -> auth
                        // Permite acesso a todos os endpoints da sua API sem autenticação
                        .requestMatchers("/api/**").permitAll()

                        // Qualquer outra requisição requer autenticação, se houver outras URLs
                        .anyRequest().authenticated()
                );

        // 3. Desabilita a página de login (que o Spring Security habilita por padrão)
        // Isso impede redirecionamentos indesejados para APIs REST
        // http.httpBasic(basic -> basic.disable()); // Opcional se você não quer autenticação básica

        return http.build();
    }
}