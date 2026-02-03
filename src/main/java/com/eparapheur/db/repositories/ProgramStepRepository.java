package com.eparapheur.db.repositories;

import com.eparapheur.db.entities.ProgramStepEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProgramStepRepository implements PanacheRepository<ProgramStepEntity> {
}

