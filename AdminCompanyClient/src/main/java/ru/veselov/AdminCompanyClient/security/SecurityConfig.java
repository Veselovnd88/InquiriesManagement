package ru.veselov.AdminCompanyClient.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@Slf4j
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Настройка filterChain");
        /*запрос отправляется по адресу http://localhost:9103/oauth2/authorization/admin-client-oidc
        * Далее оттуда уходит с кодом со статусом openid на сервер авторизации, оттуда приходит код авторизации,
        * Код перенаправляется на oauth2/token, проверяется y oauth2/jwks, далее  получаем свой токены приложения и можем его использовать
        * Перед логином - перенаправляет на страницу авторизации - по умолчанию, для входа в клиентское приложение логин происходит в
        * Скоупе OPENID (узнать подробнее)
        * Другие конфигурации клиента забираются из пропертей и передаются в RegisteredOauth2Authorizedclient */
        http.authorizeHttpRequests(request->
                request.requestMatchers(HttpMethod.GET,"/admin**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(log->
                        log.loginPage(OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI+"/admin-client-oidc"))
                .oauth2Client(Customizer.withDefaults());
        return http.build();
    }

}


