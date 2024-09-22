package com.distribuidos.authentication.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Validated
@Configuration
@Lazy(value = false)
@ConfigurationProperties(prefix = "environment")
public class EnvironmentConfig {

    @Valid
    @NotNull
    private Domains domains;

    @NotBlank
    private String serviceName;

    @NotNull
    private Integer maxPayloadSizeInMb;

    @NotNull
    private Boolean securityDisableSslCertValidation;

    @Valid
    @NotNull
    private ServiceRetry serviceRetry;

    @NotBlank
    private String operatorName;

    @NotBlank
    private String operatorId;

    @Data
    @Validated
    public static class Domains {

        @NotBlank
        private String usersDomain;

        @NotBlank
        private String centralizerDomain;
    }

    @Data
    @Validated
    public static class ServiceRetry {

        @NotNull
        private Integer maxAttempts;
    }
}
