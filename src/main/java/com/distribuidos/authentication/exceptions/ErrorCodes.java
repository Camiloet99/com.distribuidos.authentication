package com.distribuidos.authentication.exceptions;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorCodes {

    private static final String PREFIX = "AT-00";

    public static final String CENTRALIZER_UPSTREAM_ERROR = PREFIX + "01";
    public static final String CENTRALIZER_UPSTREAM_VALIDATE_USER_ERROR = PREFIX + "02";
    public static final String USER_CREATION_UPSTREAM_ERROR = PREFIX + "03";
    public static final String USER_BY_DOCUMENT_UPSTREAM_ERROR = PREFIX + "04";


}
