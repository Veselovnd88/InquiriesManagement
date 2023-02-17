package ru.veselov.AdminCompanyClient.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;
import ru.veselov.AdminCompanyClient.model.DivisionModel;
import ru.veselov.AdminCompanyClient.model.ManagerModel;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class RestManagerService {
    @Value("${resources}")
    private String resourceServer;

    private final RestTemplate restTemplate;

    @Autowired
    public RestManagerService(String accessToken) {
        /*Создание ресттемплэйт с настройкой на присоединение хедера с токеном*/
        this.restTemplate = new RestTemplate();
        if(accessToken!=null){
            this.restTemplate.getInterceptors().add(getBearerTokenInterceptor(accessToken));
        }
    }

    public List<ManagerModel> findAllManagers(){
        ManagerModel[] array = restTemplate.getForObject(resourceServer, ManagerModel[].class);
        if(array==null){
            return Collections.emptyList();
        }
        else{
            return List.of(array);
        }
    }

    public List<DivisionModel> findAllDivisions(){
        return null;
    }





    private ClientHttpRequestInterceptor getBearerTokenInterceptor(String accessToken){
        /*We need in interceptor for adding header with access token*/
        ClientHttpRequestInterceptor interceptor = new ClientHttpRequestInterceptor() {
            @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
                request.getHeaders().add("Authorization","Bearer "+accessToken);
                return execution.execute(request,body);
            }
        };
        return interceptor;
    }





}
