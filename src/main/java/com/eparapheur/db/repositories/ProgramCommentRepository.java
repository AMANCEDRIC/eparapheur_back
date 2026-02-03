package com.eparapheur.db.repositories;

import com.eparapheur.db.entities.ProgramCommentEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProgramCommentRepository implements PanacheRepository<ProgramCommentEntity> {
}

