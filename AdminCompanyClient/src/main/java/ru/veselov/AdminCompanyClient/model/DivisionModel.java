package ru.veselov.AdminCompanyClient.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"inquiries","managers"})
@AllArgsConstructor
@Builder
public class DivisionModel {
    @NotBlank(message = "Поле не может быть пустым")
    @Size(min=2, max = 2, message = "Код отдела должен состоять из 2 символов")
    private String divisionId;
    @NotBlank(message = "Поле не может быть пустым")
    @Size(min = 10, max=45, message = "Описание должно быть длиной от 10 до 45 символов")
    private String name;
    private Set<ManagerModel> managers = new HashSet<>();
   @Builder.Default private final Set<InquiryModel> inquiries=new HashSet<>();
}
