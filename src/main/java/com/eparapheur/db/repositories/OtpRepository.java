package com.eparapheur.db.repositories;

import com.eparapheur.db.entities.OtpEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OtpRepository implements PanacheRepository<OtpEntity> {
}
