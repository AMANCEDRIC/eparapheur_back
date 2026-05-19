package com.eparapheur.core.services;

import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.service.http.commons.TimestampDataLoader;
import eu.europa.esig.dss.service.tsp.OnlineTSPSource;
import eu.europa.esig.dss.spi.x509.CommonTrustedCertificateSource;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service de configuration DSS (EU Digital Signature Services).
 * Centralise la création du CertificateVerifier et du TSPSource (FreeTSA).
 * Expose un PAdESService prêt pour la signature PAdES-BASELINE-T.
 */
@ApplicationScoped
public class DssConfigService {

    private static final Logger logger = LoggerFactory.getLogger(DssConfigService.class);

    @ConfigProperty(name = "dss.tsa-url", defaultValue = "https://freetsa.org/tsr")
    String tsaUrl;

    @ConfigProperty(name = "dss.check-revocation", defaultValue = "false")
    boolean checkRevocation;

    /**
     * Construit un CertificateVerifier configuré en mode permissif pour les
     * certificats auto-signés (pas de vérification OCSP/CRL distante).
     * En production avec des certificats CA, activer les sources OCSP/CRL.
     */
    public CommonCertificateVerifier buildCertificateVerifier() {
        CommonCertificateVerifier verifier = new CommonCertificateVerifier();

        // Accepte les certificats auto-signés sans chaîne de confiance externe
        verifier.setTrustedCertSources(new CommonTrustedCertificateSource());
        verifier.setCheckRevocationForUntrustedChains(checkRevocation);

        logger.debug("DSS CertificateVerifier construit (check-revocation={})", checkRevocation);
        return verifier;
    }

    /**
     * Construit un TSPSource pointant vers FreeTSA (gratuit, RFC 3161).
     * Utilisé pour horodater la signature → niveau PAdES-BASELINE-T.
     */
    public OnlineTSPSource buildTspSource() {
        OnlineTSPSource tspSource = new OnlineTSPSource(tsaUrl);
        tspSource.setDataLoader(new TimestampDataLoader()); // HTTP client DSS intégré
        logger.debug("DSS TSPSource configuré : {}", tsaUrl);
        return tspSource;
    }

    /**
     * Construit un PAdESService DSS complet, avec horodatage TSA inclus.
     * C'est le point d'entrée principal pour la signature PAdES-BASELINE-T.
     */
    public PAdESService buildPadesService() {
        PAdESService service = new PAdESService(buildCertificateVerifier());
        service.setTspSource(buildTspSource());
        logger.debug("DSS PAdESService construit avec TSP : {}", tsaUrl);
        return service;
    }
}
