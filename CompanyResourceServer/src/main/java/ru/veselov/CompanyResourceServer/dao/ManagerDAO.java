package ru.veselov.CompanyResourceServer.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.veselov.CompanyResourceServer.entity.Division;
import ru.veselov.CompanyResourceServer.entity.ManagerEntity;


import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@Transactional(readOnly = true)
@Slf4j
public class ManagerDAO {

    @PersistenceContext
    private final EntityManager entityManager;
    private final DivisionDAO divisionDAO;

    public ManagerDAO(EntityManager entityManager, DivisionDAO divisionDAO) {
        this.entityManager = entityManager;
        this.divisionDAO = divisionDAO;
    }


    @Transactional
    public void save(ManagerEntity managerEntity){
        Optional<ManagerEntity> optionalManager = findOne(managerEntity.getManagerId());
        if(optionalManager.isPresent()){
            managerEntity=optionalManager.get();
                for(Division d: managerEntity.getDivisions()){
                    Optional<Division> one = divisionDAO.findOne(d.getDivisionId());
                    if(one.isPresent()){
                        managerEntity.addDivision(one.get());
                    }
                    else{
                        managerEntity.addDivision(d);
                    }
            }
        }
        entityManager.persist(managerEntity);
    }

    @Transactional
    public void saveWithDivisions(ManagerEntity managerEntity, Set<Division> divisionSet){
        Optional<ManagerEntity> optManager = findOne(managerEntity.getManagerId());
        if(optManager.isEmpty()) {
            for (Division d : divisionSet) {
                Optional<Division> one = divisionDAO.findOne(d.getDivisionId());
                if (one.isPresent()) {
                    managerEntity.addDivision(one.get());
                } else {
                    managerEntity.addDivision(d);
                }
            }
        }
        else{
            managerEntity=optManager.get();
            List<Division> allDivs = divisionDAO.findAll();
            for(Division d: allDivs){
                if(divisionSet.contains(d)){
                    managerEntity.addDivision(d);
                }
                else{
                    managerEntity.removeDivision(d);
                }
            }
        }
        entityManager.persist(managerEntity);
    }


    @SuppressWarnings("unchecked")
    public List<ManagerEntity> findAll(){
        return entityManager.createQuery(" SELECT m from ManagerEntity m ").getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<ManagerEntity> findAllWithDivisions(){
        return entityManager.createQuery(" SELECT m from ManagerEntity m LEFT JOIN FETCH m.divisions ").getResultList();
    }
    public Optional<ManagerEntity> findOne(Long managerId){
        ManagerEntity manager= entityManager.find(ManagerEntity.class, managerId);
        return Optional.ofNullable(manager);
    }
    @Transactional
    public ManagerEntity update(ManagerEntity manager){
        for(Division d: manager.getDivisions()){
            Optional<Division> one = divisionDAO.findOne(d.getDivisionId());
            if(one.isPresent()){
                manager.addDivision(one.get());
            }
            else{
                manager.addDivision(d);
            }
        }
        entityManager.persist(manager);
        return manager;
    }


    public Optional<ManagerEntity> findOneWithDivisions(Long managerId){
        Query namedQuery = entityManager.createNamedQuery("ManagerEntity.findManager");
        namedQuery.setParameter("id",managerId);
        ManagerEntity manager;
        try{
            manager = (ManagerEntity) namedQuery.getSingleResult();
        }
        catch (NoResultException noResultException){
            manager=null;
        }
        return Optional.ofNullable(manager);
    }
    @Transactional
    public void delete(ManagerEntity manager){
        removeDivisions(manager);
        entityManager.remove(manager);
    }

    @Transactional
    public void deleteById(Long managerId) {
        Optional<ManagerEntity> manager = findOne(managerId);
        manager.ifPresent(this::delete);
    }

    @Transactional
    public void removeDivisions(ManagerEntity manager){
        Optional<ManagerEntity> optManager = findOne(manager.getManagerId());
        if(optManager.isPresent()) {
            ManagerEntity managerEntity = optManager.get();
            Set<Division> divisions = Set.copyOf(managerEntity.getDivisions());
            for (Division d : divisions) {
                Optional<Division> one = divisionDAO.findOne(d.getDivisionId());
                if (one.isPresent()) {
                    managerEntity.removeDivision(d);
                }
            }entityManager.persist(managerEntity);
        }
    }

}
