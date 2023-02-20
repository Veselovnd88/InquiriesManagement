package ru.veselov.AdminCompanyClient.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.veselov.AdminCompanyClient.model.DivisionModel;
import ru.veselov.AdminCompanyClient.model.ManagerModel;
import ru.veselov.AdminCompanyClient.util.DivisionValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.*;

@Controller
@RequestMapping(name = "/")
@Slf4j
public class AdminController {
    @Value("${resource-uri}")
    private String resourceUri;

    private final WebClient webClient;
    private final DivisionValidator divisionValidator;
    @Autowired
    public AdminController(WebClient webClient, DivisionValidator divisionValidator) {
        this.webClient = webClient;
        this.divisionValidator = divisionValidator;
    }

    @GetMapping("/admin")
    //Administrator information page after loading showing all divisions and manager
    public String adminMainPage(@RegisteredOAuth2AuthorizedClient("admin-client-authorization-code")
                                OAuth2AuthorizedClient authorizedClient,
                                Model model)
    {
        log.trace("Запрос по адресу /admin");
/*        DivisionModel[] result= webClient.get().uri(resourceUri + "/divs")
                .attributes(ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
                .retrieve().bodyToMono(DivisionModel[].class).block();
        List<DivisionModel> divisions;
        if(result==null){
            divisions= Collections.emptyList();
        }
        else{
            divisions = List.of(result);
        }*/

        List<DivisionModel> divisions = new ArrayList<>();
        divisions.add(DivisionModel.builder().divisionId("LL").name("Hello").build());
        divisions.add(DivisionModel.builder().divisionId("LT").name("Hello2").build());
        model.addAttribute("divisions",divisions);

        List<ManagerModel> managers = new ArrayList<>();
        managers.add(ManagerModel.builder().firstName("first").lastName("Last").divisions(new HashSet<>(divisions))
                .build());

        model.addAttribute("managers",managers);
        return "adminPage";
    }

    @GetMapping("/admin/managers")
    public String managerPage(@RegisteredOAuth2AuthorizedClient("admin-client-authorization-code")
                                  OAuth2AuthorizedClient authorizedClient,
                              Model model){
        log.trace("Запрос по адресу /admin/managers");
        List<ManagerModel> managers = new ArrayList<>();
        managers.add(ManagerModel.builder().firstName("first").lastName("Last").managerId(1000L)
                .build());
        //TODO
        model.addAttribute("managers",managers);
        return "managers";
    }

    @GetMapping(value = "/admin/divisions")
    public String divisionsPage(@RegisteredOAuth2AuthorizedClient("admin-client-authorization-code")
                              OAuth2AuthorizedClient authorizedClient,
                              Model model){
        log.trace("Запрос по адресу /admin/divisions");
        List<DivisionModel> divisions = new ArrayList<>();
        divisions.add(DivisionModel.builder().divisionId("LL").name("Hello").build());
        divisions.add(DivisionModel.builder().divisionId("LT").name("Hello2").build());
        model.addAttribute("divisions", divisions);
        return "divisions";
    }
    @GetMapping(value = "/admin/divisions/create")
    public String divisionCreate(@ModelAttribute("division") DivisionModel division){
        return "divisionCreate";
    }

    @PostMapping(value = "/admin/divisions/create")
    public String createDivision(@RegisteredOAuth2AuthorizedClient("admin-client-authorization-code")
           OAuth2AuthorizedClient authorizedClient,
                                 @ModelAttribute("division") @Valid DivisionModel division, BindingResult errors){
        log.trace("POST запрос на /admin/divisions/create");
        divisionValidator.validate(division,errors);
        if(errors.hasErrors()){
            log.info("Неправильный ввод отдела");
            return "divisionCreate";
        }
        log.info("Отправка отдела {} на сохранение", division.getName());
        webClient.post().uri(resourceUri + "/divs/create")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(division), DivisionModel.class)
                .attributes(oauth2AuthorizedClient(authorizedClient))
                .retrieve().bodyToMono(DivisionModel.class).block();

        return "redirect:/admin/divisions";
    }


    @DeleteMapping(value = "/admin/divisions/delete/{id}")
    public String deleteDivision(
            @RegisteredOAuth2AuthorizedClient("admin-client-authorization-code")
                OAuth2AuthorizedClient authorizedClient,
                @PathVariable("id") String id,
                Model model){
        //TODO
        return "divisions";
    }

}
