package com.eparapheur.core.models;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class AssignPermissionsRequest {
    @NotNull(message = "La liste des permissions est obligatoire")
    @NotEmpty(message = "Au moins une permission doit être fournie")
    private List<Long> permissionIds;

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }
}
