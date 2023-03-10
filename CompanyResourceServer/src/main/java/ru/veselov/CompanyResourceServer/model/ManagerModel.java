package ru.veselov.CompanyResourceServer.model;


import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(exclude = "divisions")
@AllArgsConstructor
@Builder
public class ManagerModel {
    private Long managerId;
    private String firstName;
    private String lastName;
    private String userName;
   @Builder.Default private Set<DivisionModel> divisions = new HashSet<>();
}
