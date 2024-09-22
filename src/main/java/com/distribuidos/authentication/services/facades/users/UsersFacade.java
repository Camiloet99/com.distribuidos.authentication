package com.distribuidos.authentication.services.facades.users;

import com.distribuidos.authentication.config.EnvironmentConfig;
import com.distribuidos.authentication.exceptions.UserNotFoundException;
import com.distribuidos.authentication.exceptions.UserUpstreamException;
import com.distribuidos.authentication.models.ResponseBody;
import com.distribuidos.authentication.services.facades.users.models.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import static com.distribuidos.authentication.exceptions.ErrorCodes.USER_BY_DOCUMENT_UPSTREAM_ERROR;
import static com.distribuidos.authentication.exceptions.ErrorCodes.USER_CREATION_UPSTREAM_ERROR;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsersFacade {

    private static final ParameterizedTypeReference<ResponseBody<UserEntity>> RESPONSE_TYPE_USER =
            new ParameterizedTypeReference<>() {
            };

    private static final String FIND_USER_BY_DOCUMENT_PATH = "/users/%s";
    private static final String SAVE_USER_PATH = "/users";

    private final WebClient webClient;
    private final EnvironmentConfig environmentConfig;

    public Mono<Boolean> createUser(UserEntity userRequest) {

        String resourceUri = environmentConfig.getDomains().getUsersDomain()
                + SAVE_USER_PATH;

        return webClient
                .post()
                .uri(resourceUri)
                .bodyValue(userRequest)
                .exchangeToMono(userResponse -> {
                    HttpStatus httpStatus = HttpStatus.valueOf(userResponse.statusCode().value());
                    if (HttpStatus.OK.equals(httpStatus) || HttpStatus.CREATED.equals(httpStatus)) {
                        return just(true);
                    }

                    HttpHeaders responseHeaders = userResponse.headers().asHttpHeaders();
                    return userResponse.bodyToMono(String.class)
                            .flatMap(responseBody -> {
                                log.error("{} - The users service responded with "
                                                + "an unexpected failure response for: {}"
                                                + "\nStatus Code: {}\nResponse Headers: {}\nResponse Body: {}",
                                        USER_CREATION_UPSTREAM_ERROR, resourceUri, httpStatus, responseHeaders,
                                        responseBody);
                                return error(new UserUpstreamException(responseBody));
                            });
                })
                .retryWhen(Retry
                        .max(environmentConfig.getServiceRetry().getMaxAttempts())
                        .filter(UserUpstreamException.class::isInstance)
                        .onRetryExhaustedThrow((ignore1, ignore2) -> ignore2.failure()));

    }

    public Mono<UserEntity> findUserByDocumentId(String documentId) {

        String resourceUri = environmentConfig.getDomains().getUsersDomain()
                + String.format(FIND_USER_BY_DOCUMENT_PATH, documentId);

        return webClient
                .get()
                .uri(resourceUri)
                .exchangeToMono(userResponse -> {
                    HttpStatus httpStatus = HttpStatus.valueOf(userResponse.statusCode().value());
                    if (HttpStatus.OK.equals(httpStatus)) {
                        return userResponse.bodyToMono(RESPONSE_TYPE_USER)
                                .map(ResponseBody::getResult);
                    }

                    if (HttpStatus.NOT_FOUND.equals(httpStatus)) {
                        return Mono.error(new UserNotFoundException("User with document " + documentId
                                + " was not found."));
                    }

                    HttpHeaders responseHeaders = userResponse.headers().asHttpHeaders();
                    return userResponse.bodyToMono(String.class)
                            .flatMap(responseBody -> {
                                log.error("{} - The centralizer service responded with "
                                                + "an unexpected failure response for: {}"
                                                + "\nStatus Code: {}\nResponse Headers: {}\nResponse Body: {}",
                                        USER_BY_DOCUMENT_UPSTREAM_ERROR, resourceUri, httpStatus, responseHeaders,
                                        responseBody);
                                return error(new UserUpstreamException(responseBody));
                            });
                })
                .retryWhen(Retry
                        .max(environmentConfig.getServiceRetry().getMaxAttempts())
                        .filter(UserUpstreamException.class::isInstance)
                        .onRetryExhaustedThrow((ignore1, ignore2) -> ignore2.failure()));
    }

}
