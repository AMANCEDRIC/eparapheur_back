package com.eparapheur.db.repositories;

import com.eparapheur.db.entities.SignatureProgramEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SignatureProgramRepository implements PanacheRepository<SignatureProgramEntity> {
}

