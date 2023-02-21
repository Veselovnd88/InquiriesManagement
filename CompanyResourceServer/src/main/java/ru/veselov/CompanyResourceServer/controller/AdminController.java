package ru.veselov.CompanyResourceServer.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.veselov.CompanyResourceServer.model.DivisionModel;
import ru.veselov.CompanyResourceServer.service.CustomerService;
import ru.veselov.CompanyResourceServer.service.DivisionService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@Slf4j
public class AdminController {

    private final DivisionService divisionService;

    private final CustomerService customerService;
    @Autowired
    public AdminController(DivisionService divisionService, CustomerService customerService) {
        this.divisionService = divisionService;
        this.customerService = customerService;
    }

    @GetMapping(value = "/divs", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public @ResponseBody List<DivisionModel> getDivisions(){
        return divisionService.findAll();
    }

    @GetMapping(value = "/divs/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity <DivisionModel> getDivision(@PathVariable("id") String id){
        log.info("Запрос отдела по id {}", id);
        Optional<DivisionModel> optional = divisionService.findById(id);
        return optional.map(d-> new ResponseEntity<>(d,HttpStatus.OK))
                .orElseGet(()->new ResponseEntity<>(null,HttpStatus.NOT_FOUND));
    }
    @PostMapping(value = "/divs/create",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DivisionModel> createDivision(@RequestBody DivisionModel divisionModel){
        log.info("Сохранение отдела в БД {}", divisionModel.getDivisionId());
        Optional<DivisionModel> byId = divisionService.findById(divisionModel.getDivisionId());
        if(byId.isEmpty()){
            divisionService.save(divisionModel);
            return new ResponseEntity<>(divisionModel,HttpStatus.CREATED);
        }
        else{
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

    }

    @PostMapping(value = "/customer",produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    public ResponseEntity<?> saveCustomer(@RequestBody User customer){

        log.info("Сохранено {}", customer.getFirstName());
        //customerService.save(customer);
        return new ResponseEntity<>("customer created",HttpStatus.CREATED);
    }
}
