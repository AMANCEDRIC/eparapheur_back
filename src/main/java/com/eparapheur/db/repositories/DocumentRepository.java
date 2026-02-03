package com.eparapheur.db.repositories;

import com.eparapheur.db.entities.DocumentEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DocumentRepository implements PanacheRepository<DocumentEntity> {
}

