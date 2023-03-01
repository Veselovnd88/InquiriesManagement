package ru.veselov.AdminCompanyClient.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
/*
* http://localhost:9102/oauth2/authorize?response_type=code&client_id=admin-client
* &scope=openid&state=AMp8d4r23RY1jxZfsqzeoCwZrEcPx_16rz-fKZxqFMQ=&redirect_
* uri=http://127.0.0.1:9103/login/oauth2/code/admin-client-oidc&nonce=VvHPpdDm9a4EQh-5j69NS4RbGsfo-f5Tb43EDis7b_o&continue
* */

@Configuration
@Slf4j
@EnableWebSecurity
@EnableMethodSecurity()
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
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN")
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
        //TODO перенести в БД
        return new InMemoryClientRegistrationRepository(oidc,admin);
    }

    @Bean
    @SuppressWarnings({"unchecked","unused"})
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        /*Собираются все роли, и проверяются в зависимости от типа*/
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
            authorities.forEach(authority -> {
                if (authority instanceof OidcUserAuthority oidcUserAuthority) {
                    log.trace("Проверка роли из объекта OIDC: {}", authority);
                    OidcIdToken idToken = oidcUserAuthority.getIdToken();
                    OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();
                    List<GrantedAuthority> authoritiesFromToken = ((List<String>) idToken.getClaim("authorities")).stream()
                            .map(SimpleGrantedAuthority::new).collect(Collectors.toList());
                    mappedAuthorities.add(oidcUserAuthority);
                    mappedAuthorities.addAll(authoritiesFromToken);
                    log.trace("Добавление в общих список ролей");

                } else if (authority instanceof OAuth2UserAuthority oauth2UserAuthority) {
                    log.trace("Проверка роли из объекта Oauth2Authority : {}", authority);
                    //FIXME понять как сюда помещать асорити
                    Map<String, Object> userAttributes = oauth2UserAuthority.getAttributes();
                    mappedAuthorities.add(oauth2UserAuthority);
                    // Map the attributes found in userAttributes
                    // to one or more GrantedAuthority's and add it to mappedAuthorities
                }
                else{
                    log.trace("Добавление роли, не вошедней в предыдущие две проверки: {}",authority);
                    mappedAuthorities.add(authority);
                }
            });
            return mappedAuthorities;
        };
    }

    

}


