package ru.veselov.CompanyResourceServer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.veselov.CompanyResourceServer.dao.ManagerDAO;
import ru.veselov.CompanyResourceServer.entity.Division;
import ru.veselov.CompanyResourceServer.entity.ManagerEntity;
import ru.veselov.CompanyResourceServer.exception.NoSuchManagerException;
import ru.veselov.CompanyResourceServer.model.DivisionModel;
import ru.veselov.CompanyResourceServer.model.ManagerModel;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ManagerService {

    private final ManagerDAO managerDAO;


    public ManagerService(ManagerDAO managerDAO) {
        this.managerDAO = managerDAO;
    }

    public void save(ManagerModel manager){
        ManagerEntity entity = toManagerEntity(manager);
        managerDAO.saveWithDivisions(entity,
                    manager.getDivisions().stream()
                            .map(this::toDivisionEntity).collect(Collectors.toSet()));
        log.info("{}: сохранен/обновлен менеджер с набором отделов {}", manager.getManagerId()
                    ,manager.getDivisions());
    }

    public ManagerModel findOne(Long userId) throws NoSuchManagerException {
        Optional<ManagerEntity> one = managerDAO.findOne(userId);
        if(one.isPresent()){
            return toManagerModel(one.get());
        }
        else{
            throw new NoSuchManagerException();
        }
    }
    public List<ManagerModel> findAll(){
        return managerDAO.findAll().stream().map(this::toManagerModel).toList();
    }

    public Set<ManagerModel> findAllWithDivisions(){
        return managerDAO.findAllWithDivisions().stream().map(this::toManagerModel).collect(Collectors.toSet());
    }

    public ManagerModel findOneWithDivisions(Long userId) throws NoSuchManagerException {
        Optional<ManagerEntity> oneWithDivisions = managerDAO.findOneWithDivisions(userId);
        if(oneWithDivisions.isPresent()){
            ManagerModel managerModel = toManagerModel(oneWithDivisions.get());
            managerModel.setDivisions(oneWithDivisions.get().getDivisions().stream().map(this::toDivisionModel)
                    .collect(Collectors.toSet()));
            return managerModel;
        }
        else throw new NoSuchManagerException();
    }

    public void remove(ManagerModel managerModel){
        log.info("{}: менеджер удален из БД", managerModel.getManagerId());
        managerDAO.deleteById(managerModel.getManagerId());
    }

    public void removeDivisions(ManagerModel managerModel){
        log.info("{}: удалены все отделы у менеджера", managerModel.getManagerId());
        managerDAO.removeDivisions(toManagerEntity(managerModel));
    }




    private ManagerEntity toManagerEntity(ManagerModel manager){
        ManagerEntity managerEntity = new ManagerEntity();
        managerEntity.setManagerId(manager.getManagerId());
        managerEntity.setLastName(manager.getLastName());
        managerEntity.setFirstName(manager.getFirstName());
        managerEntity.setUserName(manager.getUserName());
        return managerEntity;
    }
    private ManagerModel toManagerModel(ManagerEntity manager){
        return ManagerModel.builder().managerId(manager.getManagerId()).userName(manager.getUserName())
                .lastName(manager.getLastName()).firstName(manager.getFirstName())
                .divisions(manager.getDivisions().stream().map(this::toDivisionModel).collect(Collectors.toSet()))
                .build();
    }
    private Division toDivisionEntity(DivisionModel divisionModel){
        Division division = new Division();
        division.setDivisionId(divisionModel.getDivisionId());
        division.setName(divisionModel.getName());
        return division;
    }
    private DivisionModel toDivisionModel(Division division){
        return DivisionModel.builder().divisionId(division.getDivisionId()).name(division.getName()).build();
    }

}
