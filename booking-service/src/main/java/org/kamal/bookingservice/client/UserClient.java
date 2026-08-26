package org.kamal.bookingservice.client;

import org.kamal.bookingservice.dto.response.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserClient {
    private final RestClient restClient;
    private final String internalApiKey;

    public UserClient(RestClient.Builder restClientBuilder,
                      @Value("${user.service.url}") String userServiceUrl,
                      @Value("${internal.api-key}") String internalApiKey) {
        this.restClient = restClientBuilder.baseUrl(userServiceUrl).build();
        this.internalApiKey = internalApiKey;
    }

    public UserResponse getUser(Long userId) {
        return restClient.get()
                .uri("/users/{id}", userId)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .body(UserResponse.class);
    }
}
