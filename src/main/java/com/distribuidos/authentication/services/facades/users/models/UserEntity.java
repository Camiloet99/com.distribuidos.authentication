package com.distribuidos.authentication.services.facades.users.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEntity {
    
    Long documentId;
    String fullName;
    String status;
    String email;
    String description;
    String password;
    String address;
    String token;
    
}
