package com.auction.product.keycloak;

import com.auction.product.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.jwt.JwtDecoder;
//import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


//@Component
//@RequiredArgsConstructor
public class KeycloakClient {

    //private final RestTemplate restTemplate;

   // private final String keycloakUrl = "http://keycloak-server/auth/realms/{realm}/users/{userId}"; // Adjust URL accordingly
/*
    public UserResponse getUserByIdFromToken(String accessToken) {

        HttpHeaders headers = createHeaders(accessToken);

        ResponseEntity<UserResponse> response = restTemplate.exchange(
                keycloakUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                UserResponse.class,
                "auction-realm",
                extractUserIdFromToken(accessToken)
        );

        return response.getBody();
    }

 */
/*
    private String extractUserIdFromToken(String accessToken) {

        JwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation("http://localhost:9098/realms/auction-realm");
        Jwt jwt = jwtDecoder.decode(accessToken);
        return jwt.getClaimAsString("sub");
    }
*/
    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }

    /*
    private final RestTemplate restTemplate;

    private final String keycloakUrl = "http://keycloak-server/auth/realms/{realm}/users/{userId}";

    public UserResponse getUserById(String userId, String accessToken) {
        HttpHeaders headers = createHeaders(accessToken);

        ResponseEntity<UserResponse> response = restTemplate.exchange(
                keycloakUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                UserResponse.class,
                "your-realm",
                userId
        );

        return response.getBody();
    }

    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }

    */
}
