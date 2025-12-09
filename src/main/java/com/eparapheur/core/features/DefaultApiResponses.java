package com.eparapheur.core.features;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A convenient meta-annotation for Swagger API responses.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@APIResponses({
        @APIResponse(responseCode = "200", description = "Success"),
        @APIResponse(responseCode = "201", description = "Created"),
        @APIResponse(responseCode = "202", description = "Accepted"),
        @APIResponse(responseCode = "204", description = "No Content"),
        @APIResponse(responseCode = "400", description = "Bad Request"),
        @APIResponse(responseCode = "401", description = "Unauthorized"),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not Found"),
        @APIResponse(responseCode = "409", description = "Conflict"),
        @APIResponse(responseCode = "500", description = "Internal Server Error"),
        @APIResponse(responseCode = "502", description = "Bad Gateway"),
        @APIResponse(responseCode = "503", description = "Service Unavailable"),
        @APIResponse(responseCode = "504", description = "Gateway Timeout")
})
public @interface DefaultApiResponses {

}
