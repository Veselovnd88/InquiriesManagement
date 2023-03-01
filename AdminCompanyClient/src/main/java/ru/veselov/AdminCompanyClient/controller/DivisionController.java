package ru.veselov.AdminCompanyClient.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.veselov.AdminCompanyClient.model.DivisionModel;
import ru.veselov.AdminCompanyClient.model.ManagerModel;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@Controller
@RequestMapping("/admin/divisions")
@Slf4j

public class DivisionController {

    private final WebClient webClient;
    private final OAuth2AuthorizedClientManager auth2AuthorizedClientManager;
    private final OAuth2AuthorizedClientService auth2AuthorizedClientService;

    @Autowired
    public DivisionController(WebClient webClient, OAuth2AuthorizedClientManager auth2AuthorizedClientManager, OAuth2AuthorizedClientService auth2AuthorizedClientService) {
        this.webClient = webClient;
        this.auth2AuthorizedClientManager = auth2AuthorizedClientManager;
        this.auth2AuthorizedClientService = auth2AuthorizedClientService;
    }

    @GetMapping()
    public String divisionsPage(Model model){
        /*Получение всех отделов, здесь OAuth2A-Client получаю из контекста,
        * и вручную помещаю в сервис*/
        log.trace("IN GET /admin/divisions");
        log.info("Получение списка отделов");
        /*Получаем аутентификацию из контекста, для получения клиента с новым ID составляем request и получаем объект из менеджера,
        * теперь его можно использовать для передачи webClient*/
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthorizeRequest request= OAuth2AuthorizeRequest.withClientRegistrationId("admin-client-code")
                .principal(authentication).build();

        OAuth2AuthorizedClient authorizedClient = auth2AuthorizedClientManager.authorize(request);
        auth2AuthorizedClientService.saveAuthorizedClient(authorizedClient,authentication);//FIXME проверить что тут будет происходить
        log.trace("Authorized name: {}",
                authorizedClient != null ? authorizedClient.getClientRegistration().getClientName() : "null");

        DivisionModel[] result= webClient.get().uri("/divisions")
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
    /*Создание отдела*/
    public String divisionCreate(@ModelAttribute("division") DivisionModel division){
        //Аннотация @ModelAttribute-> создает и помещает в модель чистый объект DivisionModel
        log.trace("IN GET /admin/divisions/create");
        return "divisionCreate";
    }

    @PostMapping(value = "/create")
    /*Создание отдела - передача готового*/
    public String createDivision(@RegisteredOAuth2AuthorizedClient("admin-client-code")
                                 OAuth2AuthorizedClient authorizedClient,
                                 @ModelAttribute("division") @Valid DivisionModel division, BindingResult errors){

        log.trace("IN POST на /admin/divisions/create");
        log.trace("Authorized name: {}, reg id: {}", authorizedClient.getPrincipalName(),authorizedClient.getClientRegistration().getClientId());
        if(errors.hasErrors()){
            log.info("Ошибка при вводе отдела: {}", errors.getAllErrors().get(0));
            return "divisionCreate";
        }
        log.info("Отправка отдела {} на сохранение", division.getName());
        Optional<DivisionModel> optional = webClient.post().uri("/divisions/create")
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
                               @RegisteredOAuth2AuthorizedClient("admin-client-code")
                               OAuth2AuthorizedClient authorizedClient){
        log.trace("IN GET /admin/divisions/{}",id);
        log.trace("Authorized name: {}, reg id: {}", authorizedClient.getPrincipalName(),authorizedClient.getClientRegistration().getClientId());

        Optional<DivisionModel> optional = webClient.get()
                .uri(uri-> uri.path("/divisions/{id}").build(id))
                .attributes(oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> Mono.empty())
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.empty())
                .bodyToMono(DivisionModel.class).blockOptional();
        /*Получаем отдел сразу с прикрепленными менеджерами*/
        /*Для проверки*/
        //FIXME
        optional.ifPresent(divisionModel -> divisionModel.setManagers(Set.of(ManagerModel.builder()
                .lastName("Vasya")
                .userName("UserPetya").build())));

        model.addAttribute("division",optional.orElse(null));
        return "divisionPage";
    }


    @DeleteMapping(value = "/delete/{id}")
    public String deleteDivision(
            @RegisteredOAuth2AuthorizedClient("admin-client-code")
            OAuth2AuthorizedClient authorizedClient,
            @PathVariable("id") String id){
        log.trace("IN /admin/divisions/delete/{}", id);

        webClient.delete().uri(uri-> uri.path("divisions/{id}").build(id))
                .attributes(oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .onStatus(HttpStatus.OK::equals,clientResponse -> Mono.empty())
                .onStatus(HttpStatusCode::is5xxServerError,clientResponse -> Mono.error(Exception::new))
                .toBodilessEntity().block();
        log.info("Отдел с ID:{} удален", id);

        return "redirect:/admin/divisions";
    }

    @GetMapping(value = "/edit/{id}")
    public String divisionCreate(@PathVariable("id") String id,
            @ModelAttribute("division") DivisionModel division,
                                 @RegisteredOAuth2AuthorizedClient("admin-client-code")
                                 OAuth2AuthorizedClient authorizedClient, Model model){
        log.trace("IN GET /admin/divisions/edit/{}",id);
        Optional<DivisionModel> optional = webClient.get()
                .uri(uri-> uri.path("/divisions/{id}").build(id))
                .attributes(oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> Mono.empty())
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.empty())
                .bodyToMono(DivisionModel.class).blockOptional();
        model.addAttribute("division", optional.orElse(null));
        return "divisionEditPage";
    }

    @PatchMapping(value = "/edit/{id}")
    public String editDivision(@RegisteredOAuth2AuthorizedClient("admin-client-code")
                                 OAuth2AuthorizedClient authorizedClient,
                                 @ModelAttribute("division") @Valid DivisionModel division, BindingResult errors,
    @PathVariable("id") String id){

        log.trace("IN PATCH на /admin/divisions/edit/{}",id);
        log.trace("Authorized name: {}, reg id: {}", authorizedClient.getPrincipalName(),authorizedClient.getClientRegistration().getClientId());
        if(errors.hasErrors()){
            log.info("Ошибка при вводе отдела: {}", errors.getAllErrors().get(0));
            return "divisionCreate";
        }
        log.info("Отправка отдела {} на изменение", division.getName());
        Optional<DivisionModel> optional = webClient.patch()
                .uri(uriBuilder -> uriBuilder.path("/divisions/edit/{id}").build(id))
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
        log.trace("Redirect to :/admin/divisions/");
        return "redirect:/admin/divisions/"+optional.get().getDivisionId();
    }



}
