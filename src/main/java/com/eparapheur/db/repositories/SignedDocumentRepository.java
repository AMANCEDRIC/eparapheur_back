package com.eparapheur.db.repositories;

import com.eparapheur.db.entities.SignedDocumentEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;

@Default
@ApplicationScoped
public class SignedDocumentRepository implements PanacheRepository<SignedDocumentEntity> {
}
