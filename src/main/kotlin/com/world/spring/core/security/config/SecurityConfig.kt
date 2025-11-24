package com.world.spring.core.security.config

import com.world.spring.core.security.jwt.JwtAuthFilter
import com.world.spring.core.security.CustomAuthEntryPoint
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableMethodSecurity
class SecurityConfig(
    // Inject the filter provider lazily — avoids creating JwtAuthFilter too early
    private val jwtAuthFilterProvider: ObjectProvider<JwtAuthFilter>
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()


    /**
     * Use AuthenticationConfiguration to obtain AuthenticationManager.
     * This avoids building DaoAuthenticationProvider here and therefore avoids
     * needing LocalUserDetailsService in this config class constructor.
     */
    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager =
        authConfig.authenticationManager

    @Bean
    fun filterChain(http: HttpSecurity,  customAuthEntryPoint: CustomAuthEntryPoint): SecurityFilterChain {
        http
            .csrf { it.disable() } // we're using JWTs, stateless
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint(customAuthEntryPoint)
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/api/auth/**",
                        "/h2-console/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html"
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .headers { headers ->
                // for H2 console frames (dev only)
                headers.frameOptions { it.disable() }
            }
            .httpBasic(Customizer.withDefaults())

        // add JWT filter before UsernamePasswordAuthenticationFilter
        // only get the filter when configuring the chain (delays bean resolution)
        val jwtAuthFilter = jwtAuthFilterProvider.getIfAvailable()
        if (jwtAuthFilter != null) {
            http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
        }
        return http.build()
    }
}