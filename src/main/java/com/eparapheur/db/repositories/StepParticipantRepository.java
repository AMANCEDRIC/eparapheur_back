package com.eparapheur.db.repositories;

import com.eparapheur.db.entities.StepParticipantEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StepParticipantRepository implements PanacheRepository<StepParticipantEntity> {
}

