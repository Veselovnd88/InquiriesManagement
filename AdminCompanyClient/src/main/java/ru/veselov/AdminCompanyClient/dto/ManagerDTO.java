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
public class ManagerDTO {
    private String firstName;
    private String lastName;
    private String userName;
    @Builder.Default private List<String> divisions = new ArrayList<>();
}
