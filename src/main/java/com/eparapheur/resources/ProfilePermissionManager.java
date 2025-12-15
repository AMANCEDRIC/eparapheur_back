package com.eparapheur.resources;

import com.eparapheur.core.features.ApiResponse;
import com.eparapheur.core.models.AssignPermissionsRequest;
import com.eparapheur.core.models.HttpContextStatus;
import com.eparapheur.db.entities.PermissionEntity;
import com.eparapheur.db.entities.ProfilUserEntity;
import com.eparapheur.db.entities.ProfilUserHasPermissionEntity;
import com.eparapheur.db.repositories.PermissionRepository;
import com.eparapheur.db.repositories.ProfilUserHasPermissionRepository;
import com.eparapheur.db.repositories.ProfilUserRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Validator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

@Path("/profiles")
@ApplicationScoped
public class ProfilePermissionManager {

    private  static final Logger logger = org.slf4j.LoggerFactory.getLogger(ProfilePermissionManager.class);


    @Inject
    ProfilUserRepository profilUserRepository;

    @Inject
    PermissionRepository permissionRepository;

    @Inject
    ProfilUserHasPermissionRepository profilUserHasPermissionRepository;

    @Inject
    Validator validator;

    /**
     * Assigner des permissions à un profil (remplace toutes les permissions existantes)
     * POST /profiles/{profileId}/permissions
     */
    @POST
    @Path("{profileId}/permissions")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response assignPermissionsToProfile(
            @PathParam("profileId") Long profileId,
            AssignPermissionsRequest request
    ) {
        ApiResponse<Object> response = new ApiResponse<>();

        // 1. Validation de la requête
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            response.setStatus_code(400);
            response.setStatus_message("Erreur de validation");
            response.setData(violations);
            return Response.status(400).entity(response).build();
        }

        // 2. Vérifier que le profil existe
        ProfilUserEntity profile = profilUserRepository.findById(profileId);
        if (profile == null) {
            response.setStatus_code(404);
            response.setStatus_message("Profil non trouvé");
            return Response.status(404).entity(response).build();
        }

        // 3. Vérifier que toutes les permissions existent
        List<Long> invalidPermissions = request.getPermissionIds().stream()
                .filter(permId -> permissionRepository.findById(permId) == null)
                .collect(Collectors.toList());

        if (!invalidPermissions.isEmpty()) {
            response.setStatus_code(400);
            response.setStatus_message("Permissions invalides: " + invalidPermissions);
            return Response.status(400).entity(response).build();
        }

        try {
            // 4. Supprimer les permissions existantes du profil
            profilUserHasPermissionRepository.delete("idProfil", profileId);
            logger.info("Permissions existantes supprimées pour le profil {}", profileId);

            // 5. Ajouter les nouvelles permissions
            List<ProfilUserHasPermissionEntity> newPermissions = request.getPermissionIds().stream()
                    .map(permissionId -> {
                        ProfilUserHasPermissionEntity puhp = new ProfilUserHasPermissionEntity();
                        puhp.setIdProfil(profileId);
                        puhp.setIdPermission(permissionId);
                        return puhp;
                    })
                    .collect(Collectors.toList());

            profilUserHasPermissionRepository.persist(newPermissions);
            logger.info("{} permissions assignées au profil {}: {}",
                    newPermissions.size(), profileId, request.getPermissionIds());

            response.setStatus_code(HttpContextStatus.SUCCESS_OPERATION);
            response.setStatus_message("Permissions assignées avec succès");
            response.setData(Map.of(
                    "profileId", profileId,
                    "profileName", profile.getLibProfil(),
                    "permissionsCount", newPermissions.size(),
                    "permissionIds", request.getPermissionIds()
            ));

            return Response.ok(response).build();

        } catch (Exception e) {
            logger.error("Erreur lors de l'assignation des permissions: {}", e.getMessage(), e);
            response.setStatus_code(500);
            response.setStatus_message("Erreur lors de l'assignation des permissions");
            return Response.status(500).entity(response).build();
        }
    }


    /**
     * Ajouter une permission à un profil (sans supprimer les autres)
     * POST /profiles/{profileId}/permissions/{permissionId}
     */
    @POST
    @Path("{profileId}/permissions/{permissionId}")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public Response addPermissionToProfile(
            @PathParam("profileId") Long profileId,
            @PathParam("permissionId") Long permissionId
    ) {
        ApiResponse<Object> response = new ApiResponse<>();

        // 1. Vérifier que le profil existe
        ProfilUserEntity profile = profilUserRepository.findById(profileId);
        if (profile == null) {
            response.setStatus_code(404);
            response.setStatus_message("Profil non trouvé");
            return Response.status(404).entity(response).build();
        }

        // 2. Vérifier que la permission existe
        PermissionEntity permission = permissionRepository.findById(permissionId);
        if (permission == null) {
            response.setStatus_code(404);
            response.setStatus_message("Permission non trouvée");
            return Response.status(404).entity(response).build();
        }

        // 3. Vérifier si la permission n'est pas déjà assignée
        ProfilUserHasPermissionEntity existing = profilUserHasPermissionRepository
                .find("idProfil = ?1 AND idPermission = ?2", profileId, permissionId)
                .firstResult();

        if (existing != null) {
            response.setStatus_code(409);
            response.setStatus_message("Cette permission est déjà assignée au profil");
            return Response.status(409).entity(response).build();
        }

        try {
            // 4. Ajouter la permission
            ProfilUserHasPermissionEntity puhp = new ProfilUserHasPermissionEntity();
            puhp.setIdProfil(profileId);
            puhp.setIdPermission(permissionId);
            profilUserHasPermissionRepository.persist(puhp);

            logger.info("Permission {} ({}) ajoutée au profil {} ({})",
                    permissionId, permission.getCode(), profileId, profile.getLibProfil());

            response.setStatus_code(HttpContextStatus.SUCCESS_OPERATION);
            response.setStatus_message("Permission ajoutée avec succès");
            response.setData(Map.of(
                    "profileId", profileId,
                    "profileName", profile.getLibProfil(),
                    "permissionId", permissionId,
                    "permissionCode", permission.getCode(),
                    "permissionName", permission.getLibPermis()
            ));

            return Response.ok(response).build();

        } catch (Exception e) {
            logger.error("Erreur lors de l'ajout de la permission: {}", e.getMessage(), e);
            response.setStatus_code(500);
            response.setStatus_message("Erreur lors de l'ajout de la permission");
            return Response.status(500).entity(response).build();
        }
    }


    /**
     * Retirer une permission d'un profil
     * DELETE /profiles/{profileId}/permissions/{permissionId}
     */
    @DELETE
    @Path("{profileId}/permissions/{permissionId}")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public Response removePermissionFromProfile(
            @PathParam("profileId") Long profileId,
            @PathParam("permissionId") Long permissionId
    ) {
        ApiResponse<Object> response = new ApiResponse<>();

        // 1. Vérifier que le profil existe
        ProfilUserEntity profile = profilUserRepository.findById(profileId);
        if (profile == null) {
            response.setStatus_code(404);
            response.setStatus_message("Profil non trouvé");
            return Response.status(404).entity(response).build();
        }

        // 2. Trouver et supprimer la relation
        ProfilUserHasPermissionEntity puhp = profilUserHasPermissionRepository
                .find("idProfil = ?1 AND idPermission = ?2", profileId, permissionId)
                .firstResult();

        if (puhp == null) {
            response.setStatus_code(404);
            response.setStatus_message("Cette permission n'est pas assignée à ce profil");
            return Response.status(404).entity(response).build();
        }

        try {
            PermissionEntity permission = permissionRepository.findById(permissionId);
            profilUserHasPermissionRepository.delete(puhp);

            logger.info("Permission {} ({}) retirée du profil {} ({})",
                    permissionId,
                    permission != null ? permission.getCode() : "unknown",
                    profileId,
                    profile.getLibProfil());

            response.setStatus_code(HttpContextStatus.SUCCESS_OPERATION);
            response.setStatus_message("Permission retirée avec succès");
            response.setData(Map.of(
                    "profileId", profileId,
                    "profileName", profile.getLibProfil(),
                    "permissionId", permissionId
            ));

            return Response.ok(response).build();

        } catch (Exception e) {
            logger.error("Erreur lors de la suppression de la permission: {}", e.getMessage(), e);
            response.setStatus_code(500);
            response.setStatus_message("Erreur lors de la suppression de la permission");
            return Response.status(500).entity(response).build();
        }
    }



    /**
     * Récupérer les permissions d'un profil
     * GET /profiles/{profileId}/permissions
     */
    @GET
    @Path("{profileId}/permissions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProfilePermissions(@PathParam("profileId") Long profileId) {
        ApiResponse<Object> response = new ApiResponse<>();

        // 1. Vérifier que le profil existe
        ProfilUserEntity profile = profilUserRepository.findById(profileId);
        if (profile == null) {
            response.setStatus_code(404);
            response.setStatus_message("Profil non trouvé");
            return Response.status(404).entity(response).build();
        }

        try {
            // 2. Récupérer les permissions
            List<ProfilUserHasPermissionEntity> profilHasPermissions =
                    profilUserHasPermissionRepository.find("idProfil", profileId).list();

            // 3. Construire la liste des permissions avec leurs détails
            List<Map<String, Object>> permissions = profilHasPermissions.stream()
                    .map(php -> {
                        PermissionEntity perm = permissionRepository.findById(php.getIdPermission());
                        if (perm != null) {
                            Map<String, Object> permMap = new HashMap<>();
                            permMap.put("id", perm.getId());
                            permMap.put("code", perm.getCode());
                            permMap.put("name", perm.getLibPermis());
                            permMap.put("isActive", perm.getIsActive());
                            return permMap;
                        }
                        return null;
                    })
                    .filter(p -> p != null)
                    .collect(Collectors.toList());

            response.setStatus_code(HttpContextStatus.SUCCESS_OPERATION);
            response.setStatus_message("Success");
            response.setData(Map.of(
                    "profileId", profileId,
                    "profileName", profile.getLibProfil(),
                    "profileDescription", profile.getDescription() != null ? profile.getDescription() : "",
                    "permissionsCount", permissions.size(),
                    "permissions", permissions
            ));

            return Response.ok(response).build();

        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des permissions: {}", e.getMessage(), e);
            response.setStatus_code(500);
            response.setStatus_message("Erreur lors de la récupération des permissions");
            return Response.status(500).entity(response).build();
        }
    }


    /**
     * Récupérer toutes les permissions disponibles
     * GET /permissions
     */
    @GET
    @Path("/permissions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPermissions() {
        ApiResponse<Object> response = new ApiResponse<>();

        try {
            List<PermissionEntity> allPermissions = permissionRepository.findAll().list();

            List<Map<String, Object>> permissions = allPermissions.stream()
                    .filter(perm -> perm.getDeleted() == null || perm.getDeleted() == 0)
                    .map(perm -> {
                        Map<String, Object> permMap = new HashMap<>();
                        permMap.put("id", perm.getId());
                        permMap.put("code", perm.getCode());
                        permMap.put("name", perm.getLibPermis());
                        permMap.put("isActive", perm.getIsActive());
                        return permMap;
                    })
                    .collect(Collectors.toList());

            response.setStatus_code(HttpContextStatus.SUCCESS_OPERATION);
            response.setStatus_message("Success");
            response.setData(Map.of(
                    "permissionsCount", permissions.size(),
                    "permissions", permissions
            ));

            return Response.ok(response).build();

        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de toutes les permissions: {}", e.getMessage(), e);
            response.setStatus_code(500);
            response.setStatus_message("Erreur lors de la récupération des permissions");
            return Response.status(500).entity(response).build();
        }
    }


}
