package com.eparapheur.db.repositories;

import com.eparapheur.db.entities.PermissionEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PermissionRepository implements PanacheRepository<PermissionEntity> {
}

