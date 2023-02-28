package ru.veselov.AdminCompanyClient.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
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
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
/*
* http://localhost:9102/oauth2/authorize?response_type=code&client_id=admin-client
* &scope=openid&state=AMp8d4r23RY1jxZfsqzeoCwZrEcPx_16rz-fKZxqFMQ=&redirect_
* uri=http://127.0.0.1:9103/login/oauth2/code/admin-client-oidc&nonce=VvHPpdDm9a4EQh-5j69NS4RbGsfo-f5Tb43EDis7b_o&continue
* */

@Configuration
@Slf4j
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Настройка filterChain");
        /*После GET запроса на адрес, перенаправляется на сервер авторизации, после логина (отправка POST с кредами)
        * после отправляется GET запрос на сервер авторизации;
        * после сервер авторизации отправляет code GET запрос по адресу http://127.0.0.1:9103/oauth2/authorization/admin-client-oidc
        * Код перенаправляется на oauth2/token, проверяется y oauth2/jwks, далее  получаем свой токены приложения и можем его использовать
        * Перед логином - перенаправляет на страницу авторизации - по умолчанию, для входа в клиентское приложение логин происходит в
        * Скоупе OPENID
        * Если использовать закомменченное DEFAULT... то не работает, т.к. есть конфликт в именах (localhost и т.д.)
        */
        http.oauth2Login(log->
                //log.loginPage(OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI+"/admin-client-oidc"))
                            log.loginPage("http://127.0.0.1:9103/oauth2/authorization/admin-client-oidc"))
                .oauth2Client(Customizer.withDefaults());
        http.authorizeHttpRequests(request->
                request
                        .anyRequest().authenticated());
        return http.build();
    }


    @Bean
    public OAuth2AuthorizedClientManager auth2AuthorizedClientManager(ClientRegistrationRepository clientRegistrationRepository,
                                                                      OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository){

        DefaultOAuth2AuthorizedClientManager manager = new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository,
                oAuth2AuthorizedClientRepository);
        manager.setAuthorizedClientProvider(auth2AuthorizedClientProvider());
        return manager;
    }

    @Bean
    OAuth2AuthorizedClientProvider auth2AuthorizedClientProvider(){
        /*в провайдере указываются необходимые нам типы AuthorizationGrant*/
        return OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode().refreshToken()
                .build();
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


    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                if (OidcUserAuthority.class.isInstance(authority)) {
                    OidcUserAuthority oidcUserAuthority = (OidcUserAuthority)authority;
                    log.warn(oidcUserAuthority.getAuthority());
                    OidcIdToken idToken = oidcUserAuthority.getIdToken();
                    OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();

                    // Map the claims found in idToken and/or userInfo
                    // to one or more GrantedAuthority's and add it to mappedAuthorities

                } else if (OAuth2UserAuthority.class.isInstance(authority)) {
                    OAuth2UserAuthority oauth2UserAuthority = (OAuth2UserAuthority)authority;
                    log.warn(oauth2UserAuthority.getAuthority());
                    Map<String, Object> userAttributes = oauth2UserAuthority.getAttributes();

                    // Map the attributes found in userAttributes
                    // to one or more GrantedAuthority's and add it to mappedAuthorities

                }
            });

            return mappedAuthorities;
        };
    }

    

}


