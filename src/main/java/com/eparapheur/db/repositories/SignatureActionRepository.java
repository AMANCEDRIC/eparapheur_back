package com.eparapheur.db.repositories;

import com.eparapheur.db.entities.SignatureActionEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;

@Default
@ApplicationScoped
public class SignatureActionRepository implements PanacheRepository<SignatureActionEntity> {
}
