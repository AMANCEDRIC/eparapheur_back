package com.eparapheur.db.repositories;

import com.eparapheur.db.entities.UserCertificateEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;

@Default
@ApplicationScoped
public class UserCertificateRepository implements PanacheRepository<UserCertificateEntity> {
}
