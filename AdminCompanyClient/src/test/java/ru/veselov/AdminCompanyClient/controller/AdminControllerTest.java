package ru.veselov.AdminCompanyClient.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;

@WebMvcTest(controllers = AdminController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ClientAutoConfiguration.class
})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebClient webClient;

    @MockBean
    private OAuth2AuthorizedClientService auth2AuthorizedClientService;

    @MockBean
    private OAuth2AuthorizedClient client;


    @BeforeEach
    void init(){
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("test").build();
        OAuth2AccessToken accessToken= new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,"test", Instant.now(),Instant.now());
        client = new OAuth2AuthorizedClient(clientRegistration,null,accessToken);
    }

    @Test
    @Disabled
    public void adminPageTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/admin"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("adminPage"));
                }
}