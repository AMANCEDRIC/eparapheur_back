package com.eparapheur.db.repositories;

import com.eparapheur.db.entities.SignatureProgramEntity;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SignatureProgramRepository implements PanacheRepository<SignatureProgramEntity> {

    /**
     * Trouve les programmes initiés par un utilisateur spécifique
     */
    public PanacheQuery<SignatureProgramEntity> findByInitiator(Long accountId) {
        return find("idInitiatorAccount = ?1 ORDER BY createdAt DESC", accountId);
    }

    /**
     * Trouve les programmes où l'utilisateur est soit l'initiateur, soit un participant
     */
    public PanacheQuery<SignatureProgramEntity> findInvolvedByUser(Long accountId) {
        return find("SELECT DISTINCT p FROM SignatureProgramEntity p " +
                   "LEFT JOIN p.steps s " +
                   "LEFT JOIN s.participants part " +
                   "WHERE p.idInitiatorAccount = ?1 " +
                   "OR part.idAccount = ?1 " +
                   "ORDER BY p.createdAt DESC", accountId);
    }
}


