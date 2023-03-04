package ru.veselov.CompanyResourceServer.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.veselov.CompanyResourceServer.model.DivisionModel;
import ru.veselov.CompanyResourceServer.model.ManagerModel;
import ru.veselov.CompanyResourceServer.service.ManagerService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("api/managers")
@Slf4j
@AllArgsConstructor
public class ManagerController {
    private final ManagerService managerService;




    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public @ResponseBody Set<ManagerModel> getManagers(){
        log.trace("IN GET /api/managers");
        log.info("Получение списка менеджеров");
        return managerService.findAllWithDivisions();
    }


}
