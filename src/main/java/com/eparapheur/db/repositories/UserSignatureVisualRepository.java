package com.eparapheur.db.repositories;

import com.eparapheur.db.entities.UserSignatureVisualEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;

@Default
@ApplicationScoped
public class UserSignatureVisualRepository implements PanacheRepository<UserSignatureVisualEntity> {
}
