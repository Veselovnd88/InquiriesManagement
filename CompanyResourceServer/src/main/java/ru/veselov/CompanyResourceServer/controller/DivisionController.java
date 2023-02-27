package ru.veselov.CompanyResourceServer.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.veselov.CompanyResourceServer.exception.NoSuchDivisionException;
import ru.veselov.CompanyResourceServer.model.DivisionModel;
import ru.veselov.CompanyResourceServer.service.CustomerService;
import ru.veselov.CompanyResourceServer.service.DivisionService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/divisions")
@Slf4j
public class DivisionController {

    private final DivisionService divisionService;

    private final CustomerService customerService;
    @Autowired
    public DivisionController(DivisionService divisionService, CustomerService customerService) {
        this.divisionService = divisionService;
        this.customerService = customerService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public @ResponseBody List<DivisionModel> getDivisions(){
        return divisionService.findAll();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity <DivisionModel> getDivision(@PathVariable("id") String id){
        log.info("Запрос отдела по id {}", id);
        try {
            DivisionModel oneWithManagers = divisionService.findOneWithManagers(id);
            return new ResponseEntity<>(oneWithManagers,HttpStatus.OK);
        } catch (NoSuchDivisionException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping(value = "/create",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DivisionModel> createDivision(@RequestBody DivisionModel divisionModel){
        log.trace("IN /api/divisions/create");
        Optional<DivisionModel> byId = divisionService.findById(divisionModel.getDivisionId());
        if(byId.isEmpty()){
            divisionService.save(divisionModel);
            log.info("Сохранение отдела в БД {}", divisionModel.getDivisionId());
            return new ResponseEntity<>(divisionModel,HttpStatus.CREATED);
        }
        else{
            log.info("Объект не сохранен");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @PatchMapping(value = "/edit/{id}",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DivisionModel> editDivision(@RequestBody DivisionModel divisionModel,
                                                      @PathVariable("id") String id){
        /*id - то что редактируем, объект - новое имя*/
        Optional<DivisionModel> byId = divisionService.findById(divisionModel.getDivisionId());
        Optional<DivisionModel> current = divisionService.findById(id);
        if(byId.isEmpty()){
            log.info("Изменение отдела в БД {}", divisionModel.getDivisionId());
            if(current.isPresent()){
                DivisionModel currentModel = current.get();
                currentModel.setDivisionId(divisionModel.getDivisionId());
                divisionService.save(currentModel);
            }
            else{
                divisionService.save(divisionModel);}
            return new ResponseEntity<>(divisionModel,HttpStatus.CREATED);
        }

        else{
            log.info("Ошибка редактирования отдела в БД {}", divisionModel.getDivisionId());
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteDivision(@PathVariable("id") String id){
        log.trace("IN Delete /api/divs/delete/{}",id);
        log.info("Удаление отдела с ID:{}", id);
        Optional<DivisionModel> byId = divisionService.findById(id);
        byId.ifPresent(divisionService::remove);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PostMapping(value = "/customer",produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    public ResponseEntity<?> saveCustomer(@RequestBody User customer){

        log.info("Сохранено {}", customer.getFirstName());
        //customerService.save(customer);
        return new ResponseEntity<>("customer created",HttpStatus.CREATED);
    }
}
