package com.hostelhub.security;

public final class SecurityConstants {

    private SecurityConstants() {}

    public static final String ACCESS_COOKIE = "HOSTELHUB_ACCESS";
    public static final String CSRF_COOKIE = "XSRF-TOKEN";
    public static final String CSRF_HEADER = "X-XSRF-TOKEN";
}
