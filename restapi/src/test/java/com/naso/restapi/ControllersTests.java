package com.naso.restapi;

import com.naso.restapi.controllers.AuthorizationController;
import com.naso.restapi.security.jwt.JwtResponse;
import com.naso.restapi.security.jwt.JwtUtils;
import com.naso.restapi.security.jwt.TokenRefreshResponse;
import com.naso.restapi.service.MyAnimeListService;
import com.naso.restapi.service.ProfileService;
import com.naso.restapi.service.ShikimoriService;
import jakarta.mail.MessagingException;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.hibernate.validator.internal.util.Contracts.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ControllersTests {
    @Mock
    private ProfileService profileService;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private ShikimoriService shikimoriService;
    @Mock
    private MyAnimeListService myAnimeListService;
    @InjectMocks
    private AuthorizationController authorizationController;

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void correctSignInTest() throws IOException {
        final String expectedResult = "{\"isSucceeded\":true,\"message\":\"Success\",\"data\":{\"accessToken\":\"a\",\"refreshToken\":\"r\",\"type\":\"Bearer\",\"profileId\":1}}";
        final String mail = "Arthur";
        final String password = "Sidorov";


        when(profileService.authenticateUser(mail, password)).thenReturn(new JwtResponse("a", "r", 1));

        final String actualResult = authorizationController.signIn("{\"mail\": \"Arthur\", \"password\": \"Sidorov\"}");

        assertNotNull(actualResult);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    @Test
    void correctRegistrationTest() throws IOException, MessagingException {
        final String expectedResult = "{\"isSucceeded\":true,\"message\":\"Success\",\"data\":\"\"}";
        final String actualResult = authorizationController.addProfile("{\"mail\": \"naso\", \"mail\": \"animator@mail.ru\", \"password\": \"password\"}");

        assertNotNull(actualResult);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    @Test
    void correctActivationTest() throws IOException {
        final String expectedResult = "{\"isSucceeded\":true,\"message\":\"Success\",\"data\":true}";
        final String activationCode = "activation";

        when(profileService.activateUser(activationCode)).thenReturn(true);

        final String actualResult = authorizationController.activate("activation");

        assertNotNull(actualResult);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    @Test
    void correctRefreshTokenTest() throws IOException {
        final String expectedResult = "{\"isSucceeded\":true,\"message\":\"Success\",\"data\":{\"accessToken\":\"accessToken\",\"refreshToken\":\"refreshToken\",\"type\":\"Bearer\"}}";
        final String refreshToken = "refresh-token";

        when(profileService.refreshToken(refreshToken)).thenReturn(new TokenRefreshResponse("accessToken", "refreshToken"));

        final String actualResult = authorizationController.refreshToken("refresh-token");

        assertNotNull(actualResult);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    @Test
    void correctAuthorizationShikimoriTest() throws IOException {
        final String expectedResult = "{\"isSucceeded\":true,\"message\":\"Success\",\"data\":{\"map\":{\"tokens\":\"tokens\"}}}";
        final String code = "code";
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("name", "password", Collections.emptyList());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        String mail = authentication.getName();

        when(jwtUtils.getJwt()).thenReturn(authentication);
        when(shikimoriService.authorization(code, mail)).thenReturn("{\"tokens\":\"tokens\"}");

        final String actualResult = authorizationController.authorizationShikimori("code");

        assertNotNull(actualResult);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    @Test
    void correctAuthorizationMyAnimeListTest() throws IOException {
        final String expectedResult = "{\"isSucceeded\":true,\"message\":\"Success\",\"data\":{\"map\":{\"tokens\":\"tokens\"}}}";
        final String code = "code";
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("name", "password", Collections.emptyList());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        String mail = authentication.getName();

        when(jwtUtils.getJwt()).thenReturn(authentication);
        when(myAnimeListService.authorization(code, mail)).thenReturn("{\"tokens\":\"tokens\"}");

        final String actualResult = authorizationController.authorizationMyAnimeList("code");

        assertNotNull(actualResult);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    @Test
    void correctSignOutTest() throws IOException, MessagingException {
        final String expectedResult = "{\"isSucceeded\":true,\"message\":\"Success\",\"data\":\"Log-out complete\"}";
        final String actualResult = authorizationController.signOut("token");

        assertNotNull(actualResult);
        Assertions.assertEquals(expectedResult, actualResult);
    }
}
