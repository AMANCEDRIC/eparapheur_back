package com.eparapheur.db.repositories;

import com.eparapheur.db.entities.ProfilUserEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProfilUserRepository implements PanacheRepository<ProfilUserEntity> {
}
