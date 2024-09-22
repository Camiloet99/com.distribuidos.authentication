package com.distribuidos.authentication.services.facades.centralizer;

import com.distribuidos.authentication.config.EnvironmentConfig;
import com.distribuidos.authentication.exceptions.CentralizerValidateUserException;
import com.distribuidos.authentication.exceptions.CitizenCreationException;
import com.distribuidos.authentication.services.facades.centralizer.models.RegisterCitizenRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import static com.distribuidos.authentication.exceptions.ErrorCodes.CENTRALIZER_UPSTREAM_ERROR;
import static org.springframework.http.MediaType.ALL;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;

@Slf4j
@Component
@RequiredArgsConstructor
public class CentralizerFacade {

    private static final String VALIDATE_USER_PATTERN = "/validateCitizen/%s";
    private static final String REGISTER_USER_PATH = "/registerCitizen";

    private final WebClient webClient;
    private final EnvironmentConfig environmentConfig;

    public Mono<Boolean> validateUser(String userDocumentId) {

        String resourceUri = environmentConfig.getDomains().getCentralizerDomain()
                + String.format(VALIDATE_USER_PATTERN, userDocumentId);

        return webClient
                .get()
                .uri(resourceUri)
                .exchangeToMono(userValidationResponse -> {
                    HttpStatus httpStatus = HttpStatus.valueOf(userValidationResponse.statusCode().value());
                    if (HttpStatus.OK.equals(httpStatus)) {
                        log.warn("Auth failed - User already exists in external system");
                        return just(false);
                    }

                    if (HttpStatus.NOT_FOUND.equals(httpStatus)) {
                        log.info("User {} is not part of any external system", userDocumentId);
                        return just(true);
                    }

                    HttpHeaders responseHeaders = userValidationResponse.headers().asHttpHeaders();
                    return userValidationResponse.bodyToMono(String.class)
                            .flatMap(responseBody -> {
                                log.error("{} - The centralizer service responded with "
                                                + "an unexpected failure response for: {}"
                                                + "\nStatus Code: {}\nResponse Headers: {}\nResponse Body: {}",
                                        CENTRALIZER_UPSTREAM_ERROR, resourceUri, httpStatus, responseHeaders,
                                        responseBody);
                                return error(new CentralizerValidateUserException(responseBody));
                            });
                })
                .retryWhen(Retry
                        .max(environmentConfig.getServiceRetry().getMaxAttempts())
                        .filter(CentralizerValidateUserException.class::isInstance)
                        .onRetryExhaustedThrow((ignore1, ignore2) -> ignore2.failure()));
    }

    public Mono<Boolean> registerCitizen(RegisterCitizenRequest request) {

        String requestUri = environmentConfig.getDomains().getCentralizerDomain()
                + REGISTER_USER_PATH;

        return webClient
                .post()
                .uri(requestUri)
                .bodyValue(request)
                .exchangeToMono(createCitizenResponse -> {
                    HttpStatus httpStatus = HttpStatus.valueOf(createCitizenResponse.statusCode().value());
                    if (HttpStatus.OK.equals(httpStatus)) {
                        return just(true);
                    }

                    HttpHeaders responseHeaders = createCitizenResponse.headers().asHttpHeaders();
                    return createCitizenResponse.bodyToMono(String.class)
                            .flatMap(responseBody -> {
                                log.error("{} - The centralizer service responded with "
                                                + "an unexpected failure response for: {}"
                                                + "\nStatus Code: {}\nResponse Headers: {}\nResponse Body: {}",
                                        CENTRALIZER_UPSTREAM_ERROR, requestUri, httpStatus, responseHeaders,
                                        responseBody);
                                return error(new CitizenCreationException(request.getId().toString()));
                            });
                })
                .retryWhen(Retry
                        .max(environmentConfig.getServiceRetry().getMaxAttempts())
                        .filter(CentralizerValidateUserException.class::isInstance)
                        .onRetryExhaustedThrow((ignore1, ignore2) -> ignore2.failure()));
    }


}
