package ru.veselov.AdminCompanyClient.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.veselov.AdminCompanyClient.model.DivisionModel;

import java.util.Optional;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Component
@Slf4j
public class DivisionValidator implements Validator {

    @Value("${resource-uri}")
    private String resourceUri;

    private final WebClient webClient;
    @Autowired
    public DivisionValidator(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return DivisionModel.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        log.trace("Валидация объекта DivisionModel");
        DivisionModel divisionModel = (DivisionModel) target;
        /*WebClient делает get запрос по адресу переданному в uri,
        * передаются аттрибуты авторизованного клиента (access token)
        * При получении указанных статусов, отдаем пустой Optional - */
        Optional<DivisionModel> optional = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(resourceUri+"/divs/{id}")
                        .build(divisionModel.getDivisionId()))
                .attributes(clientRegistrationId("admin-client-authorization-code"))
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> Mono.empty())
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.empty())
                .bodyToMono(DivisionModel.class).blockOptional();
        if(optional.isPresent()){
            log.trace("Выдача ошибки");
            errors.rejectValue("divisionId","","Такой код уже существует");
        }
        log.trace("Валидация успешна, такого ID базе еще нет");
    }

}
