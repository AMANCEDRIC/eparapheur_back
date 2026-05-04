package com.eparapheur.db.repositories;

import com.eparapheur.db.entities.UserPrivateKeyEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserPrivateKeyRepository implements PanacheRepository<UserPrivateKeyEntity> {
}
