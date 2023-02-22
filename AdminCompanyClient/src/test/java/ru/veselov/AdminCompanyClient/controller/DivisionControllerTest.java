package ru.veselov.AdminCompanyClient.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;
@WebMvcTest(controllers = AdminController.class)
@WithMockUser(username = "admin", password = "hate666")
@AutoConfigureWebMvc
class DivisionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebClient webClient;





}