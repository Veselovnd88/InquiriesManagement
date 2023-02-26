package ru.veselov.AdminCompanyClient.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
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

    @Bean
    public OAuth2AuthorizedClientManager auth2AuthorizedClientManager(ClientRegistrationRepository clientRegistrationRepository,
                                                                      OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository){
        /*в провайдере указываются необходимые нам типы AuthorizationGrant*/
        OAuth2AuthorizedClientProvider provider = OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode().refreshToken().build();
        DefaultOAuth2AuthorizedClientManager manager = new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository,
                oAuth2AuthorizedClientRepository);
        manager.setAuthorizedClientProvider(provider);
        return manager;
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(){

        ClientRegistration oidc = ClientRegistration.withRegistrationId("admin-client-oidc")
                .clientId("admin-client")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .tokenUri("http://localhost:9102/oauth2/token")
                .authorizationUri("http://localhost:9102/oauth2/authorize")
                .redirectUri("http://127.0.0.1:9103/login/oauth2/code/admin-client-oidc")
                .issuerUri("http://localhost:9102")
                .jwkSetUri("http://localhost:9102/oauth2/jwks")
                .scope(OidcScopes.OPENID)
                .build();

        ClientRegistration admin = ClientRegistration.withRegistrationId("admin-client-code")
                .clientId("admin-client")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .tokenUri("http://localhost:9102/oauth2/token")
                .authorizationUri("http://localhost:9102/oauth2/authorize")
                .redirectUri("http://127.0.0.1:9103/authorized")
                .issuerUri("http://localhost:9102")
                .jwkSetUri("http://localhost:9102/oauth2/jwks")
                .scope("admin")
                .build();

        return new InMemoryClientRegistrationRepository(oidc,admin);
    }


    

}


