package ru.veselov.AdminCompanyClient.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.veselov.AdminCompanyClient.model.DivisionModel;
import ru.veselov.AdminCompanyClient.util.DivisionValidator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@Controller
@RequestMapping("/admin/divisions")
@Slf4j
public class DivisionController {

    @Value("${resource-uri}")
    private String resourceUri;

    private final WebClient webClient;
    @Autowired
    public DivisionController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping()
    public String divisionsPage(@RegisteredOAuth2AuthorizedClient("admin-client-authorization-code")
                                OAuth2AuthorizedClient authorizedClient,
                                Model model){
        log.trace("IN GET /admin/divisions");
        log.trace("Authorized name: {}, reg id: {}", authorizedClient.getPrincipalName(),authorizedClient.getClientRegistration().getClientName());
        log.trace("Получение списка отделов");
        DivisionModel[] result= webClient.get().uri(resourceUri + "/divs")
                .attributes(oauth2AuthorizedClient(authorizedClient))
                .retrieve().bodyToMono(DivisionModel[].class).block();
        List<DivisionModel> divisions;
        if(result==null){
            divisions= Collections.emptyList();
        }
        else{
            divisions = List.of(result);
        }
        model.addAttribute("divisions", divisions);
        return "divisions";
    }

    @GetMapping(value = "/create")
    public String divisionCreate(@ModelAttribute("division") DivisionModel division){
        log.trace("IN GET /admin/divisions/create");
        return "divisionCreate";
    }

    @PostMapping(value = "/create")
    public String createDivision(@RegisteredOAuth2AuthorizedClient("admin-client-authorization-code")
                                 OAuth2AuthorizedClient authorizedClient,
                                 @ModelAttribute("division") @Valid DivisionModel division, BindingResult errors){

        log.trace("IN POST на /admin/divisions/create");
        log.trace("Authorized name: {}, reg id: {}", authorizedClient.getPrincipalName(),authorizedClient.getClientRegistration().getClientId());
        if(errors.hasErrors()){
            log.info("Ошибка при вводе отдела: {}", errors.getAllErrors().get(0));
            return "divisionCreate";
        }
        log.info("Отправка отдела {} на сохранение", division.getName());
        Optional<DivisionModel> optional = webClient.post().uri(resourceUri + "/divs/create")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(division), DivisionModel.class)
                .attributes(oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.empty())
                .onStatus(HttpStatus.FORBIDDEN::equals, clientResponse -> Mono.empty())
                .bodyToMono(DivisionModel.class).blockOptional();

        if(optional.isEmpty()){
            errors.rejectValue("divisionId","","Отдел с таким ID уже существует");
            log.info("Отдел с таким ID:{} уже существует",division.getDivisionId());
            return "divisionCreate";
        }
        log.trace("Redirect to :/admin/divisions");
        return "redirect:/admin/divisions";
    }

    @GetMapping(value = "/{id}")
    public String showDivision(@PathVariable("id") String id, Model model,
                               @RegisteredOAuth2AuthorizedClient("admin-client-authorization-code")
                               OAuth2AuthorizedClient authorizedClient){
        log.trace("IN GET /admin/divisions/{}",id);
        log.trace("Authorized name: {}, reg id: {}", authorizedClient.getPrincipalName(),authorizedClient.getClientRegistration().getClientId());

        Optional<DivisionModel> optional = webClient.get()
                .uri(resourceUri+"/divs/"+id)
                .attributes(clientRegistrationId("admin-client-authorization-code"))
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> Mono.empty())
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.empty())
                .bodyToMono(DivisionModel.class).blockOptional();

        model.addAttribute("division",optional.orElse(null));
        return "divisionPage";
    }


    @DeleteMapping(value = "/delete/{id}")
    public String deleteDivision(
            @RegisteredOAuth2AuthorizedClient("admin-client-authorization-code")
            OAuth2AuthorizedClient authorizedClient,
            @PathVariable("id") String id,
            Model model){
        log.trace("IN /admin/divisions/delete/{}", id);

        webClient.delete().uri(resourceUri+"divs/"+id)
                .attributes(clientRegistrationId("admin-client-authorization-code"))
                .retrieve()
                .onStatus(HttpStatus.OK::equals,clientResponse -> Mono.empty())
                .onStatus(HttpStatusCode::is5xxServerError,clientResponse -> Mono.error(Exception::new))
                .toBodilessEntity().block();
        log.info("Отдел с ID:{} удален", id);

        return "redirect:/admin/divisions";
    }



}
