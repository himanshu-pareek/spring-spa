package com.example.spring_spa.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationManagerFactories;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authorization.EnableMultiFactorAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;

@Configuration
@EnableMultiFactorAuthentication(authorities = {})
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    var mfa = AuthorizationManagerFactories.multiFactor()
        .requireFactors(FactorGrantedAuthority.OTT_AUTHORITY,
            FactorGrantedAuthority.PASSWORD_AUTHORITY)
        .build();

    http.authorizeHttpRequests(
        authz ->
            authz
                .requestMatchers("/app2/**").access(mfa.authenticated())
                .anyRequest().authenticated()
    );

    http.formLogin(Customizer.withDefaults());
    http.oneTimeTokenLogin(Customizer.withDefaults());

    // CSRF Configuration for single-page-applications
    http.csrf(CsrfConfigurer::spa);

    return http.build();
  }

  @Bean
  OneTimeTokenGenerationSuccessHandler oneTimeTokenGenerationSuccessHandler() {
    return (request, response, oneTimeToken) -> {
      System.out.println("Click here to login - http://localhost:8080/login/ott?token=" + oneTimeToken.getTokenValue());
      response.sendRedirect("/login/ott");
    };
  }

  @Bean
  UserDetailsService userDetailsService() {
    UserDetails alice = User.withUsername("alice")
        .password("{noop}password")
        .authorities("ROLE_ADMIN", "ROLE_USER", "books.read", "books.list", "books.edit")
        .build();
    UserDetails bob = User.withUsername("bob")
        .password("{noop}password")
        .authorities("ROLE_USER", "books.list", "books.read")
        .build();
    return new InMemoryUserDetailsManager(alice, bob);
  }
}
