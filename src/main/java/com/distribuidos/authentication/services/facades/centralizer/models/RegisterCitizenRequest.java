package com.distribuidos.authentication.services.facades.centralizer.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterCitizenRequest {

    Long id;
    String name;
    String address;
    String email;
    String operatorId;
    String operatorName;

}
