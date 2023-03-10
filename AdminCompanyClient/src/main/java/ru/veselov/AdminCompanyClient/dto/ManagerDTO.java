package ru.veselov.AdminCompanyClient.dto;

import lombok.*;
import ru.veselov.AdminCompanyClient.model.DivisionModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(exclude = "divisions")
@AllArgsConstructor
@Builder
@ToString
public class ManagerDTO {
    private Long managerId;
    private String firstName;
    private String lastName;
    private String userName;
    @Builder.Default private Set<String> divisions = new HashSet<>();
}
