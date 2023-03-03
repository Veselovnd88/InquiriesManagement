package ru.veselov.AdminCompanyClient.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import ru.veselov.AdminCompanyClient.dto.ManagerDTO;
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
                               OAuth2AuthorizedClient authorizedClient,
                              @ModelAttribute("divisions") ArrayList<DivisionModel> checked){
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
        Map<DivisionModel, Boolean> responsibleDivisions = all.stream().collect(Collectors.toMap(d -> d, divisions::contains));
        model.addAttribute("manager",manager);

        // model.addAttribute("divMap",responsibleDivisions);

        log.warn(responsibleDivisions.toString());
        return "managerPage";
    }

    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable("id") String id,
                           @RegisteredOAuth2AuthorizedClient("admin-client-code")
                           OAuth2AuthorizedClient authorizedClient, Model model,
                           @ModelAttribute("manager")ManagerModel managerModel){

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
        Map<DivisionModel, Boolean> responsibleDivisions = all.stream().collect(Collectors.toMap(d -> d, divisions::contains));
        model.addAttribute("divMap",responsibleDivisions);
        model.addAttribute("array",new ArrayList<DivisionModel>());
        model.addAttribute("manager",manager);
        return "managerEditPage";
    }

    @PatchMapping(value = "/edit/{id}")
    public String editDivision(@RegisteredOAuth2AuthorizedClient("admin-client-code")
                               OAuth2AuthorizedClient authorizedClient,
                               @ModelAttribute("manager") ManagerModel managerModel, BindingResult errors,
                               @PathVariable("id") String id) {
        log.trace("IN PATCH /admin/managers/edit/{}",id);

        log.trace(managerModel.toString());
        return "managerPage";
    }
    }
