package com.eparapheur.db.repositories;

import com.eparapheur.db.entities.ProfilUserHasPermissionEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProfilUserHasPermissionRepository implements PanacheRepository<ProfilUserHasPermissionEntity> {
}

