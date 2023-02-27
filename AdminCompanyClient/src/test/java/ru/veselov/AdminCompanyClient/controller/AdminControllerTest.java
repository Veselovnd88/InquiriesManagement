package ru.veselov.AdminCompanyClient.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Client;

@WebMvcTest(controllers = AdminController.class)
@WithMockUser(username = "admin", password = "hate666")
@AutoConfigureWebMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebClient webClient;



    @Test
    @WithMockUser
    public void adminPageTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/admin")
                        .with(oauth2Client("admin-client-code")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("adminPage"))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Страница администратора")));
                }
}