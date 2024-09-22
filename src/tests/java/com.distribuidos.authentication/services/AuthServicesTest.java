package com.distribuidos.authentication.services;

import com.distribuidos.authentication.config.EnvironmentConfig;
import com.distribuidos.authentication.exceptions.CentralizerValidateUserException;
import com.distribuidos.authentication.exceptions.UserAlreadyExistsException;
import com.distribuidos.authentication.models.LoginRequest;
import com.distribuidos.authentication.security.JwtUtil;
import com.distribuidos.authentication.services.facades.centralizer.CentralizerFacade;
import com.distribuidos.authentication.services.facades.centralizer.models.RegisterCitizenRequest;
import com.distribuidos.authentication.services.facades.users.UsersFacade;
import com.distribuidos.authentication.services.facades.users.models.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class AuthServicesTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CentralizerFacade centralizerFacade;

    @Mock
    private UsersFacade usersFacade;

    @Mock
    private EnvironmentConfig environmentConfig;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAuthRegisterUser_UserValidAndNotExists() {
        UserEntity user = UserEntity.builder()
                .documentId(123L)
                .password("password")
                .build();

        UserEntity encodedUser = user.toBuilder()
                .password("encodedPassword")
                .build();

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(centralizerFacade.validateUser(anyString())).thenReturn(Mono.just(true));
        when(usersFacade.findUserByDocumentId(anyString())).thenReturn(Mono.empty());
        when(usersFacade.createUser(any(UserEntity.class))).thenReturn(Mono.just(true));
        when(centralizerFacade.registerCitizen(any(RegisterCitizenRequest.class))).thenReturn(Mono.just(true));

        Mono<Boolean> result = authService.authRegisterUser(user);

        StepVerifier.create(result)
                .expectComplete();
    }

    @Test
    public void testAuthRegisterUser_UserValidAndExists() {
        UserEntity user = UserEntity.builder()
                .documentId(123L)
                .password("password")
                .build();

        UserEntity existingUser = UserEntity.builder()
                .documentId(123L)
                .build();

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(centralizerFacade.validateUser(anyString())).thenReturn(Mono.just(true));
        when(usersFacade.findUserByDocumentId(anyString())).thenReturn(Mono.just(existingUser));

        Mono<Boolean> result = authService.authRegisterUser(user);

        StepVerifier.create(result)
                .expectError(UserAlreadyExistsException.class)
                .verify();
    }

    @Test
    public void testAuthRegisterUser_UserNotValid() {
        UserEntity user = UserEntity.builder()
                .documentId(123L)
                .password("password")
                .build();

        when(centralizerFacade.validateUser(anyString())).thenReturn(Mono.just(false));

        when(usersFacade.findUserByDocumentId(anyString())).thenReturn(Mono.empty());

        Mono<Boolean> result = authService.authRegisterUser(user);

        StepVerifier.create(result)
                .expectError(CentralizerValidateUserException.class)
                .verify();
    }

    @Test
    public void testAuthLoginUser_Success() {
        LoginRequest loginRequest = LoginRequest.builder()
                .document("123")
                .password("password")
                .build();

        UserEntity userEntity = UserEntity.builder()
                .documentId(123L)
                .password("encodedPassword")
                .build();

        when(usersFacade.findUserByDocumentId(anyString())).thenReturn(Mono.just(userEntity));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString())).thenReturn("token");

        Mono<UserEntity> result = authService.authLoginUser(loginRequest);

        StepVerifier.create(result)
                .expectNextMatches(user -> user.getToken().equals("token"))
                .verifyComplete();
    }

    @Test
    public void testAuthLoginUser_InvalidPassword() {
        LoginRequest loginRequest = LoginRequest.builder()
                .document("123")
                .password("password")
                .build();

        UserEntity userEntity = UserEntity.builder()
                .documentId(123L)
                .password("encodedPassword")
                .build();

        when(usersFacade.findUserByDocumentId(anyString())).thenReturn(Mono.just(userEntity));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        Mono<UserEntity> result = authService.authLoginUser(loginRequest);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    public void testAuthLoginUser_UserNotFound() {
        LoginRequest loginRequest = LoginRequest.builder()
                .document("123")
                .password("password")
                .build();

        when(usersFacade.findUserByDocumentId(anyString())).thenReturn(Mono.empty());

        Mono<UserEntity> result = authService.authLoginUser(loginRequest);

        StepVerifier.create(result)
                .verifyComplete();
    }

}
