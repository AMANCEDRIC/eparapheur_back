package com.eparapheur.core.features;

import com.eparapheur.core.models.AuthRequest;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

public interface IAuthentifiable {

    @Path("login")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Connexion au compte.",
            description = "Permet l'obtention d'un token de session.")
    @APIResponse(
            responseCode = "200",
            description = "success",
            content = {
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            }
    )
    Response login(AuthRequest body);

    @Path("refresh-access-token")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Rafraichir token.",
            description = "Rafraichir token de session.")
    @APIResponse(
            responseCode = "200",
            description = "success",
            content = {
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            }
    )
    Response refreshToken();


    @Path("logout")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    Response logout();
}
