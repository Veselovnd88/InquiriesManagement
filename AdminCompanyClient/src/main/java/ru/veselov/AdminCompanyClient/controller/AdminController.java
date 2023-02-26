package ru.veselov.AdminCompanyClient.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.veselov.AdminCompanyClient.model.ManagerModel;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(name = "/")
@Slf4j
public class AdminController {

    @GetMapping("/admin")
    //Administrator information page
    public String adminMainPage()
    {
        log.trace("IN GET Запрос по адресу /admin");
        return "adminPage";
    }

    @GetMapping("/admin/managers")
    public String managerPage(@RegisteredOAuth2AuthorizedClient("admin-client-code")
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


}
