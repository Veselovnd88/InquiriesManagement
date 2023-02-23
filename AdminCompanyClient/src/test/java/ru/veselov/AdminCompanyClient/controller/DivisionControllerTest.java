package ru.veselov.AdminCompanyClient.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Arrays;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.reactive.function.client.WebClient;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import ru.veselov.AdminCompanyClient.model.DivisionModel;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Client;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.web.reactive.function.client.WebClient.*;

@WebMvcTest(controllers = DivisionController.class)
@WithMockUser(username = "admin", password = "hate666")
@AutoConfigureWebMvc
class DivisionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebClient webClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RequestHeadersUriSpec requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
    private RequestBodyUriSpec requestBodyUriSpec = mock(RequestBodyUriSpec.class);
    private RequestBodySpec requestBodySpec = mock(RequestBodySpec.class);
    private RequestHeadersSpec requestHeadersSpec = mock(RequestHeadersSpec.class);
    ResponseSpec responseSpec = mock(ResponseSpec.class);
    @BeforeEach
    void init(){

    }

    @Test
    void divisionPageWithDivisionsInDbTest() throws Exception {
        DivisionModel[] array = Arrays.array(
                DivisionModel.builder()
                        .divisionId("LL")
                        .name("PP")
                        .build()
        );
        setUpWebClientGet(array);
        mockMvc.perform(get("/admin/divisions")
            .with(oauth2Client("admin-client-authorization-code")))
                    .andExpect(status().isOk())
                    .andExpect(view().name("divisions"))
                    .andExpect(content().string(Matchers.containsString("Управление")))
                .andExpect(content().string(Matchers.containsString("PP")))
        ;
    }

    @Test
    void divisionPageWithNoDivisions() throws Exception {
        setUpWebClientGet(null);
        mockMvc.perform(get("/admin/divisions")
                        .with(oauth2Client("admin-client-authorization-code")))
                .andExpect(status().isOk())
                .andExpect(view().name("divisions"))
                .andExpect(content().string(Matchers.containsString("Управление")))
                .andExpect(content().string(Matchers.containsString("Отделы еще не добавлены")))
        ;
    }

    @Test
    void getDivisionCreateTest() throws Exception {
        mockMvc.perform(get("/admin/divisions/create")
                .with(oauth2Client("admin-client-authorization-code")))
                .andExpect(status().isOk())
                .andExpect(view().name("divisionCreate"))
                .andExpect(content().string(Matchers.containsString("Создание")));
    }

    @Test
    void postDivisionCreateTest() throws Exception {
        DivisionModel name = DivisionModel.builder().divisionId("OK").name("OK").build();
        setUpWebClientPost(name);
        mockMvc.perform(post("/admin/divisions/create")
                        .param("divisionId","LL")
                        .param("name","PPadfasdfasdfasdfasdf")
                        .with(csrf())
                .with(oauth2Client("admin-client-authorization-code")))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/divisions"));
    }


    private void setUpWebClientGet(DivisionModel[] array){
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.attributes(any(Consumer.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        Mono<DivisionModel[]> mono = mock(Mono.class);
        when(mono.block()).thenReturn(array);
        when(responseSpec.bodyToMono(DivisionModel[].class)).thenReturn(mono);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
    }

    private void setUpWebClientPost(DivisionModel divisionModel){

        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(),anyString())).thenReturn(requestBodySpec);

        when(requestBodySpec.body(any(Publisher.class), (Class<?>) any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.attributes(any(Consumer.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(Predicate.class),any(Function.class))).thenReturn(responseSpec);
        Mono<DivisionModel> mono = mock(Mono.class);
        when(responseSpec.bodyToMono(DivisionModel.class)).thenReturn(mono);
        when(mono.blockOptional()).thenReturn(Optional.ofNullable(divisionModel));
        when(webClient.post()).thenReturn(requestBodyUriSpec);
    }






}