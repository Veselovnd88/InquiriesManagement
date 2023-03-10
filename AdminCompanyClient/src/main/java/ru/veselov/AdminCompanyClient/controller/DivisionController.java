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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
        /*?????????????????? ???????? ??????????????, ?????????? OAuth2A-Client ?????????????? ???? ??????????????????,
        * ?? ?????????????? ?????????????? ?? ????????????*/
        log.trace("IN GET /admin/divisions");
        log.info("?????????????????? ???????????? ??????????????");
        /*???????????????? ???????????????????????????? ???? ??????????????????, ?????? ?????????????????? ?????????????? ?? ?????????? ID ???????????????????? request ?? ???????????????? ???????????? ???? ??????????????????,
        * ???????????? ?????? ?????????? ???????????????????????? ?????? ???????????????? webClient*/
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthorizeRequest request= OAuth2AuthorizeRequest.withClientRegistrationId("admin-client-code")
                .principal(authentication).build();

        OAuth2AuthorizedClient authorizedClient = auth2AuthorizedClientManager.authorize(request);
        auth2AuthorizedClientService.saveAuthorizedClient(authorizedClient,authentication);
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
    public String divisionCreate(@ModelAttribute("division") DivisionModel division){
        /*
        ?????????????????? @ModelAttribute-> ?????????????? ?? ???????????????? ?? ???????????? ???????????? ???????????? DivisionModel*/
        log.trace("IN GET /admin/divisions/create");
        log.trace("Creation Page of division");
        return "divisionCreate";
    }

    @PostMapping(value = "/create")
    public String createDivision(@RegisteredOAuth2AuthorizedClient("admin-client-code")
                                 OAuth2AuthorizedClient authorizedClient,
                                 @ModelAttribute("division") @Valid DivisionModel division, BindingResult errors){
        /*???????????????? ???????????? - ???????????????? ???????????????? ??????????????*/
        log.trace("IN POST ???? /admin/divisions/create");
        log.trace("Authorized name: {}, reg id: {}", authorizedClient.getPrincipalName(),authorizedClient.getClientRegistration().getClientId());
        if(errors.hasErrors()){
            log.info("???????????? ?????? ?????????? ????????????: {}", errors.getAllErrors().get(0));
            return "divisionCreate";
        }
        log.info("???????????????? ???????????? {} ???? ????????????????????", division.getName());
        Optional<DivisionModel> optional = webClient.post().uri("/divisions/create")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(division), DivisionModel.class)
                .attributes(oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.empty())
                .onStatus(HttpStatus.FORBIDDEN::equals, clientResponse -> Mono.empty())
                .bodyToMono(DivisionModel.class).blockOptional();

        if(optional.isEmpty()){
            errors.rejectValue("divisionId","","?????????? ?? ?????????? ID ?????? ????????????????????");
            log.info("?????????? ?? ?????????? ID:{} ?????? ????????????????????",division.getDivisionId());
            return "divisionCreate";
        }
        log.trace("Redirect to :/admin/divisions");
        return "redirect:/admin/divisions";
    }

    @GetMapping(value = "/{id}")
    public String showDivision(@PathVariable("id") String id, Model model,
                               @RegisteredOAuth2AuthorizedClient("admin-client-code")
                               OAuth2AuthorizedClient authorizedClient){
        log.info("Return page of single division");
        log.trace("IN GET /admin/divisions/{}",id);
        log.trace("Authorized name: {}, reg id: {}", authorizedClient.getPrincipalName(),authorizedClient.getClientRegistration().getClientId());
        Optional<DivisionModel> divisionModel = getDivisionModelFromResourceServer(authorizedClient, id);
        /*???????????????? ?????????? ?????????? ?? ???????????????????????????? ??????????????????????*/
        model.addAttribute("division",divisionModel.orElse(null));
        return "divisionPage";
    }


    @DeleteMapping(value = "/delete/{id}")
    public String deleteDivision(
            @RegisteredOAuth2AuthorizedClient("admin-client-code")
            OAuth2AuthorizedClient authorizedClient,
            @PathVariable("id") String id){

        log.trace("IN /admin/divisions/delete/{}", id);
        log.trace("Deleting division method ");
        webClient.delete().uri(uri-> uri.path("divisions/{id}").build(id))
                .attributes(oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .onStatus(HttpStatus.OK::equals,clientResponse -> Mono.empty())
                .onStatus(HttpStatusCode::is5xxServerError,clientResponse -> Mono.error(Exception::new))
                .toBodilessEntity().block();
        log.info("?????????? ?? ID:{} ????????????", id);

        return "redirect:/admin/divisions";
    }

    @GetMapping(value = "/edit/{id}")
    public String divisionCreate(@PathVariable("id") String id,
            @ModelAttribute("division") DivisionModel division,
                                 @RegisteredOAuth2AuthorizedClient("admin-client-code")
                                 OAuth2AuthorizedClient authorizedClient, Model model){

        log.trace("IN GET /admin/divisions/edit/{}",id);
        log.trace("Show edit division page for id:{}",id);
        Optional<DivisionModel> divisionModel = getDivisionModelFromResourceServer(authorizedClient, id);
        model.addAttribute("division", divisionModel.orElse(null));
        return "divisionEditPage";
    }

    @PatchMapping(value = "/edit/{id}")
    public String editDivision(@RegisteredOAuth2AuthorizedClient("admin-client-code")
                                 OAuth2AuthorizedClient authorizedClient,
                                 @ModelAttribute("division") @Valid DivisionModel division, BindingResult errors,
                                 @PathVariable("id") String id){

        log.trace("IN PATCH ???? /admin/divisions/edit/{}",id);
        log.trace("Authorized name: {}, reg id: {}", authorizedClient.getPrincipalName(),authorizedClient.getClientRegistration().getClientId());
        log.trace("Edit division method, id: {}", id);

        if(errors.hasErrors()){
            log.info("???????????? ?????? ?????????? ????????????: {}", errors.getAllErrors().get(0));
            return "divisionCreate";
        }
        log.info("???????????????? ???????????? {} ???? ??????????????????", division.getName());
        Optional<DivisionModel> optional = webClient.patch()
                .uri(uriBuilder -> uriBuilder.path("/divisions/edit/{id}").build(id))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(division), DivisionModel.class)
                .attributes(oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.empty())
                .onStatus(HttpStatus.FORBIDDEN::equals, clientResponse -> Mono.empty())
                .bodyToMono(DivisionModel.class).blockOptional();
        //FIXME ?????????????? ?????? ?????????? ?????????? ?????? ????????????????????
        //FIXME ?????????????????? ???????????? ?????????????????? ?????? ????????????
        if(optional.isEmpty()){
            errors.rejectValue("divisionId","","?????????? ?? ?????????? ID ?????? ????????????????????");
            log.info("?????????? ?? ?????????? ID:{} ?????? ????????????????????",division.getDivisionId());
            return "divisionCreate";
        }

        log.trace("Redirect to :/admin/divisions/");
        return "redirect:/admin/divisions/"+optional.get().getDivisionId();
    }

    private Optional<DivisionModel> getDivisionModelFromResourceServer(@RegisteredOAuth2AuthorizedClient("admin-client-code")
                                                                       OAuth2AuthorizedClient authorizedClient,
                                                                       String id){
        return webClient.get()
                .uri(uri-> uri.path("/divisions/{id}").build(id))
                .attributes(oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> Mono.empty())
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.empty())
                .bodyToMono(DivisionModel.class).blockOptional();
    }



}
