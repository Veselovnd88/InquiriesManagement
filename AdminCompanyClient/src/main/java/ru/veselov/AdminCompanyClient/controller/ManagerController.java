package ru.veselov.AdminCompanyClient.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.function.client.WebClient;
import ru.veselov.AdminCompanyClient.model.DivisionModel;
import ru.veselov.AdminCompanyClient.model.ManagerModel;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/managers")
@AllArgsConstructor
@Slf4j
public class ManagerController {

    private final WebClient webClient;

    @GetMapping()
    public String managersPage(@RegisteredOAuth2AuthorizedClient("admin-client-code")
                              OAuth2AuthorizedClient authorizedClient,
                              Model model){
        log.trace("IN GET /admin/managers");
        Set<DivisionModel> divisions = Set.of(
                DivisionModel.builder().divisionId("VV").name("VV").build(),
                DivisionModel.builder().divisionId("V1").name("V1").build()
        );
        List<ManagerModel> managers = new ArrayList<>();
        managers.add(ManagerModel.builder().firstName("first").lastName("Last").managerId(1000L).divisions(divisions)
                .build());
        managers.add(ManagerModel.builder().firstName("second").lastName("va").managerId(1001L).divisions(divisions)
                .build());
        //TODO страничка менеджеров
        model.addAttribute("managers",managers);
        return "managers";
    }


    @GetMapping(value = "/{id}")
    public String showManager(@PathVariable("id") String id, Model model,
                               @RegisteredOAuth2AuthorizedClient("admin-client-code")
                               OAuth2AuthorizedClient authorizedClient){
        log.info("Return page of single manager");
        log.trace("IN GET /admin/managers/{}",id);
        log.trace("Authorized name: {}, reg id: {}", authorizedClient.getPrincipalName(),authorizedClient.getClientRegistration().getClientId());

        Set<DivisionModel> divisions = Set.of(
                DivisionModel.builder().divisionId("VV").name("megavasya").build(),
                DivisionModel.builder().divisionId("V1").name("megapetya").build()
        );
        ManagerModel manager = ManagerModel.builder().firstName("first").lastName("Last").managerId(1000L).divisions(divisions)
                .build();
        Set<DivisionModel> all = new HashSet<>(divisions);
        all.add(
                DivisionModel.builder().divisionId("V3").name("additional").build()
        );


        /*
        Optional<DivisionModel> optional = webClient.get()
                .uri(uri-> uri.path("/divisions/{id}").build(id))
                .attributes(oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> Mono.empty())
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.empty())
                .bodyToMono(DivisionModel.class).blockOptional();*/
        /*Получаем отдел сразу с прикрепленными менеджерами*/
        /*Для проверки*/
        //FIXME
        Map<DivisionModel,Boolean> responsibleDivisions = new HashMap<>();

        for(var d: all){
            if(divisions.contains(d)){
                responsibleDivisions.put(d,true);
            }
            else {
                responsibleDivisions.put(d,false);
            }
        }

        model.addAttribute("manager",manager);
        model.addAttribute("divMap",responsibleDivisions);
        return "managerPage";
    }
}
