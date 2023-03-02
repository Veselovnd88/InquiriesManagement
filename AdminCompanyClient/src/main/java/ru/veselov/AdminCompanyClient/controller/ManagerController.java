package ru.veselov.AdminCompanyClient.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.veselov.AdminCompanyClient.model.DivisionModel;
import ru.veselov.AdminCompanyClient.model.ManagerModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin/managers")
@Slf4j
public class ManagerController {

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
}
