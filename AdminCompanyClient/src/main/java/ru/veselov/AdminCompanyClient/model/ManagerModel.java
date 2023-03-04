package ru.veselov.AdminCompanyClient.model;


import lombok.*;
import ru.veselov.AdminCompanyClient.dto.ManagerDTO;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(exclude = "divisions")
@AllArgsConstructor
@Builder
@ToString
public class ManagerModel {
    private Long managerId;
    private String firstName;
    private String lastName;
    private String userName;
   @Builder.Default private Set<DivisionModel> divisions = new HashSet<>();

   public static ManagerDTO convertToDTO(ManagerModel model){
       return ManagerDTO.builder()
               .managerId(model.managerId)
               .divisions(model.divisions.stream().map(DivisionModel::getDivisionId).collect(Collectors.toSet()))
               .firstName(model.firstName)
               .lastName(model.lastName)
               .userName(model.userName)
               .build();
   }

}
