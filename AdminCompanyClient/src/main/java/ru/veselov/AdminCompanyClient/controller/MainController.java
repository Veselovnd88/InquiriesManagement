package ru.veselov.AdminCompanyClient.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import ru.veselov.AdminCompanyClient.model.DivisionModel;
import ru.veselov.AdminCompanyClient.model.ManagerModel;

import java.util.*;

@Controller
@RequestMapping(name = "/")
@Slf4j
public class MainController {
    @Value("${resource-uri}")
    private String resourceUri;

    private final WebClient webClient;
    @Autowired
    public MainController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/admin")
    //Administrator information page after loading showing all divisions and manager
    public String adminMainPage(@RegisteredOAuth2AuthorizedClient("admin-client-authorization-code")
                                OAuth2AuthorizedClient authorizedClient,
                                Model model)
    {
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
}