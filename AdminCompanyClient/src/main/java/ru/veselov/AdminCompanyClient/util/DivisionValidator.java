package ru.veselov.AdminCompanyClient.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.client.WebClient;
import ru.veselov.AdminCompanyClient.model.DivisionModel;

import java.util.Arrays;
import java.util.Optional;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.*;

@Component
@Slf4j
public class DivisionValidator implements Validator {

    private final WebClient webClient;
    private final OAuth2AuthorizedClientManager auth2AuthorizedClientManager;
    @Autowired
    public DivisionValidator(WebClient webClient, OAuth2AuthorizedClientManager auth2AuthorizedClientManager) {
        this.webClient = webClient;
        this.auth2AuthorizedClientManager = auth2AuthorizedClientManager;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return DivisionModel.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        log.trace("Валидация объекта DivisionModel");
        DivisionModel divisionModel = (DivisionModel) target;

        Optional<DivisionModel> optional = webClient.get()
                .uri("http://localhost:9101/api/divs/dd")//FIXME неправильно отправляет запрос
                //.uri(uriBuilder -> uriBuilder
                        /*.path("http://localhost:9101/api/divs/{id}")
                        .build(divisionModel.getDivisionId()))*/
                .attributes(clientRegistrationId("admin-client-authorization-code"))
                .retrieve().bodyToMono(DivisionModel.class).blockOptional();

        if(optional.isPresent()){
            log.trace("Код существует");
            errors.rejectValue("division","Такой код уже существует");
        }
        log.trace("Кода не существует");
    }

}
