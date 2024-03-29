package com.example.securityjwt.config;

import com.example.securityjwt.jwt.JWTFilter;
import com.example.securityjwt.jwt.JWTUtil;
import com.example.securityjwt.jwt.LoginFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    //AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil) {

        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
    }

    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //csrf disable : 세션 사용 안하니까
        http
                // rest api 설정
                .csrf(AbstractHttpConfigurer::disable) // // csrf 비활성화 -> cookie를 사용하지 않으면 꺼도 된다. (cookie를 사용할 경우 httpOnly(XSS 방어), sameSite(CSRF 방어)로 방어해야 한다.)
//                .cors(AbstractHttpConfigurer::disable) // cors 비활성화 -> 프론트와 연결 시 따로 설정 필요
                .formLogin(AbstractHttpConfigurer::disable) // 기본 login form 비활성화
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP 기본 인증을 비활성화
                .cors((cors) -> cors
                        .configurationSource(new CorsConfigurationSource() {
                            @Override
                            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                                CorsConfiguration configuration = new CorsConfiguration();

                                configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000")); // 프론트 서버
                                configuration.setAllowedMethods(Collections.singletonList("*")); //get,post,put 등의 메서드
                                configuration.setAllowCredentials(true); // 앞단에서 Credential 설정을 하면 true 로 설정해야된다.
                                configuration.setAllowedHeaders(Collections.singletonList("*")); // 허용할 헤더
                                configuration.setMaxAge(3600L); // 허용 물고있을 시간

                                configuration.setExposedHeaders(Collections.singletonList("Authorization")); // 서버에서 프론트로 JWT 보낼 때 Authorization에 넣어서 보내주기 때문

                                return configuration;
                            }
                        }))

                //경로별 인가 작업
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/login", "/", "/join").permitAll() // 모두 접근 가능
                        .requestMatchers("/admin").hasRole("ADMIN") // Admin 만 접근 가능
                        .anyRequest().authenticated()) // 그 외 나머지

                //JWTFilter 등록
                .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class)

                //필터 추가 LoginFilter()는 인자를 받음 (AuthenticationManager() 메소드에 authenticationConfiguration 객체를 넣어야 함) 따라서 등록 필요
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class)

                //세션 설정
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}