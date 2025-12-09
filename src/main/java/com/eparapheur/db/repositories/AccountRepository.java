package com.eparapheur.db.repositories;

import com.eparapheur.db.entities.AccountEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;

@Default
@ApplicationScoped
public class AccountRepository implements PanacheRepository<AccountEntity> {
}
