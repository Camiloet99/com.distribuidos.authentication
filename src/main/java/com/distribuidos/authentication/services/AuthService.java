package com.distribuidos.authentication.services;

import com.distribuidos.authentication.config.EnvironmentConfig;
import com.distribuidos.authentication.exceptions.CentralizerValidateUserException;
import com.distribuidos.authentication.exceptions.UserAlreadyExistsException;
import com.distribuidos.authentication.models.LoginRequest;
import com.distribuidos.authentication.services.facades.centralizer.CentralizerFacade;
import com.distribuidos.authentication.services.facades.centralizer.models.RegisterCitizenRequest;
import com.distribuidos.authentication.services.facades.users.UsersFacade;
import com.distribuidos.authentication.services.facades.users.models.UserEntity;
import com.distribuidos.authentication.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CentralizerFacade centralizerFacade;
    private final UsersFacade usersFacade;
    private final EnvironmentConfig environmentConfig;

    private RegisterCitizenRequest mapUserRequest(UserEntity user) {
        return RegisterCitizenRequest.builder()
                .id(user.getDocumentId())
                .address(user.getAddress())
                .name(user.getFullName())
                .email(user.getEmail())
                .operatorName(environmentConfig.getOperatorName())
                .operatorId(environmentConfig.getOperatorId())
                .build();
    }

    public Mono<Boolean> authRegisterUser(UserEntity user) {
        UserEntity encodedUser = user.toBuilder()
                .password(passwordEncoder.encode(user.getPassword()))
                .build();

        Mono<Boolean> isUserValidMono = centralizerFacade.validateUser(user.getDocumentId().toString());
        Mono<UserEntity> userAlreadyExistsMono = usersFacade.findUserByDocumentId(user.getDocumentId().toString())
                .defaultIfEmpty(UserEntity.builder().build());

        return Mono.zip(isUserValidMono, userAlreadyExistsMono)
                .flatMap(tuple -> {
                    Boolean isUserValid = tuple.getT1();
                    UserEntity userExist = tuple.getT2();

                    if (isUserValid) {
                        if (userExist.getDocumentId() == null) {
                            return usersFacade.createUser(encodedUser)
                                    .flatMap(createdUser -> centralizerFacade
                                            .registerCitizen(mapUserRequest(encodedUser)))
                                    .thenReturn(true);
                        } else {
                            return Mono.error(new UserAlreadyExistsException(user.getDocumentId().toString()));
                        }
                    } else {
                        return Mono.error(new CentralizerValidateUserException(user.getDocumentId().toString()));
                    }
                });
    }

    public Mono<UserEntity> authLoginUser(LoginRequest loginRequest) {

        log.info("Authenticating user " + loginRequest.getDocument());

        return usersFacade.findUserByDocumentId(loginRequest.getDocument())
                .filter(userEntity -> passwordEncoder.matches(loginRequest.getPassword(), userEntity.getPassword()))
                .map(userEntity -> {
                    String token = jwtUtil.generateToken(userEntity.getDocumentId().toString());
                    return userEntity.toBuilder()
                            .token(token)
                            .build();
                });
    }
}
