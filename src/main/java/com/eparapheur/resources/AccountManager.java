package com.eparapheur.resources;


import com.eparapheur.core.CrudEndPointImpl;
import com.eparapheur.core.features.ApiResponse;
import com.eparapheur.core.features.IAuthentifiable;
import com.eparapheur.core.models.AuthRequest;
import com.eparapheur.core.models.CreateUserRequest;
import com.eparapheur.core.models.HttpContextStatus;
import com.eparapheur.core.models.PaginatedResponse;
import com.eparapheur.core.models.RegisterRequest;
import com.eparapheur.core.models.OtpVerifyRequest;
import com.eparapheur.core.models.ValidateAccountRequest;
import com.eparapheur.core.services.EmailService;
import com.eparapheur.core.services.OtpService;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.qute.Template;
import io.quarkus.qute.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.eparapheur.db.entities.AccountEntity;
import com.eparapheur.db.entities.PersonEntity;
import com.eparapheur.db.entities.ProfilUserEntity;
import com.eparapheur.db.repositories.AccountRepository;

import com.eparapheur.db.repositories.PersonRepository;
import com.eparapheur.db.repositories.ProfilUserRepository;
import io.quarkus.panache.common.Page;
import io.smallrye.jwt.build.Jwt;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Validator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/accounts")
public class AccountManager extends CrudEndPointImpl<AccountEntity> implements IAuthentifiable {

    @Inject
    AccountRepository accountRepository;

    @Inject
    PersonRepository personRepository;

    @Inject
    ProfilUserRepository profilUserRepository;

    @Inject
    EmailService emailService;

    @Inject
    OtpService otpService;

    @Inject
    @Location("validation")
    Template validationTemplate;

    @Inject
    Validator validator;

    private static final Logger logger = LoggerFactory.getLogger(AccountManager.class);

    @PostConstruct
    void init() {
        // branche ton CrudEndPointImpl sur le repository
        setT(accountRepository);
    }


    //Make a simple get method to see if this controller is accessible it should returns a json object with a message
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() {
        ApiResponse<Object> response = new ApiResponse<>();
        response.setStatus_code(HttpContextStatus.SUCCESS_OPERATION);
        response.setStatus_message("Welcome to Account Manager API");
        return Response.status(200).entity(response).build();
    }
    // ================== AUTH ==================

    @Override
    @Transactional
    public Response login(AuthRequest body) {
        ApiResponse<Object> response = new ApiResponse<>();

        // 1. Validation
        var violations = validator.validate(body);
        if (!violations.isEmpty()) {
            response.setStatus_code(400);
            response.setStatus_message("Erreur de validation");
            response.setData(violations);
            return Response.status(400).entity(response).build();
        }

        // 2. Recherche du compte par login (email / username)
        AccountEntity account = accountRepository
                .find("loginCmpt", body.getLogin())
                .firstResult();

        if (account == null) {
            response.setStatus_code(401);
            response.setStatus_message("Login ou mot de passe incorrect");
            return Response.status(401).entity(response).build();
        }

        // 3. Vérification du mot de passe (hashé avec BCrypt)
        if (account.getMpCmpt() == null || !BcryptUtil.matches(body.getPassword(), account.getMpCmpt())) {
            response.setStatus_code(401);
            response.setStatus_message("Login ou mot de passe incorrect");
            return Response.status(401).entity(response).build();
        }

        // 4. Vérifier si le compte est actif
        if (Boolean.FALSE.equals(account.getActive())) {
            response.setStatus_code(403);
            response.setStatus_message("Compte inactif");
            return Response.status(403).entity(response).build();
        }

        // 5. Vérifier le profil pour déterminer si 2FA est requis
        String profilLib = null;
        if (account.getIdProfil() != null) {
            ProfilUserEntity profil = profilUserRepository.findById(account.getIdProfil());
            if (profil != null) {
                profilLib = profil.getLibProfil();
            }
        }

        // 6. Si profil AE ou ADMIN : générer et envoyer OTP (2FA requis)
        if ("AE".equals(profilLib) || "ADMIN".equals(profilLib)) {
            try {
                // Générer un token temporaire unique pour cette session 2FA
                String tempToken = java.util.UUID.randomUUID().toString();
                
                // Stocker le token temporaire dans le compte
                account.setSessionToken(tempToken);
                accountRepository.persist(account);
                accountRepository.flush(); // Forcer l'écriture immédiate en base
                logger.info("Token temporaire stocké pour accountId={}: {}", account.getId(), tempToken);
                
                // Invalider les anciens OTP pour ce compte
                otpService.invalidatePreviousOtps(account.getId(), "TWO_FACTOR_AUTH");
                
                // Générer un nouveau code OTP
                String otpCode = otpService.generateOtp();
                
                // Créer et sauvegarder l'OTP
                otpService.createOtp(account.getId(), otpCode, "TWO_FACTOR_AUTH", "EMAIL");
                
                // Récupérer le nom de l'utilisateur pour l'email
                PersonEntity person = personRepository.findById(account.getIdUser());
                String userName = person != null ? person.getPrenUser() : null;
                
                // Envoyer l'OTP par email avec le template
                emailService.sendOtpEmail(account.getLoginCmpt(), otpCode, userName);
                
                // Retourner réponse avec requiresOtp = true et le token temporaire
                Map<String, Object> data = new HashMap<>();
                data.put("requiresOtp", true);
                data.put("token", tempToken);  // Token temporaire au lieu de accountId
                data.put("message", "Un code OTP a été envoyé à votre email");
                
                response.setStatus_code(HttpContextStatus.SUCCESS_OTP_SEND);
                response.setStatus_message("Code OTP envoyé");
                response.setData(data);
                return Response.ok(response).build();
                
            } catch (Exception e) {
                logger.error("Erreur lors de la génération/envoi de l'OTP: {}", e.getMessage(), e);
                response.setStatus_code(HttpContextStatus.SERVER_ERROR_OTP_SEND_FAIL);
                response.setStatus_message("Erreur lors de l'envoi du code OTP");
                return Response.status(500).entity(response).build();
            }
        }

        // 7. Pour les autres profils : connexion directe (pas de 2FA)
        String token = generateToken(account);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("accountId", account.getId());

        response.setStatus_code(7000);
        response.setStatus_message("Success");
        response.setData(data);

        return Response.ok(response).build();
    }

    @Override
    public Response refreshToken() {
        ApiResponse<Object> response = new ApiResponse<>();
        response.setStatus_code(501);
        response.setStatus_message("Not implemented yet");
        return Response.status(501).entity(response).build();
    }

    @Override
    public Response logout() {
        ApiResponse<Object> response = new ApiResponse<>();
        response.setStatus_code(200);
        response.setStatus_message("Logged out (stateless JWT)");
        return Response.ok(response).build();
    }

    /**
     * Endpoint pour vérifier l'OTP et compléter l'authentification 2FA
     */
    @POST
    @Path("verify-otp")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response verifyOtp(OtpVerifyRequest body) {
        ApiResponse<Object> response = new ApiResponse<>();

        // 1. Validation
        var violations = validator.validate(body);
        if (!violations.isEmpty()) {
            response.setStatus_code(400);
            response.setStatus_message("Erreur de validation");
            response.setData(violations);
            return Response.status(400).entity(response).build();
        }

        // 2. Chercher le compte par token temporaire
        AccountEntity account = accountRepository
                .find("sessionToken = ?1", body.getToken())
                .firstResult();
        
        if (account == null) {
            logger.warn("Token non trouvé: {}", body.getToken());
            response.setStatus_code(404);
            response.setStatus_message("Token invalide ou expiré");
            return Response.status(404).entity(response).build();
        }
        
        logger.info("Compte trouvé par token: accountId={}", account.getId());

        // 3. Vérifier l'OTP
        com.eparapheur.db.entities.OtpEntity otp = otpService.verifyOtp(
                account.getId(),
                body.getOtp(),
                "TWO_FACTOR_AUTH"
        );

        if (otp == null) {
            response.setStatus_code(HttpContextStatus.ERROR_OTP_EXPIRED);
            response.setStatus_message("Code OTP invalide, expiré ou déjà utilisé");
            return Response.status(400).entity(response).build();
        }

        // 4. Marquer l'OTP comme utilisé
        otpService.markAsUsed(otp);

        // 5. Générer le token JWT final
        String jwtToken = generateToken(account);

        // 6. Nettoyer le token temporaire (usage unique)
        account.setSessionToken(null);
        accountRepository.persist(account);

        Map<String, Object> data = new HashMap<>();
        data.put("token", jwtToken);
        data.put("accountId", account.getId());
        data.put("message", "Authentification réussie");

        response.setStatus_code(7000);
        response.setStatus_message("Success");
        response.setData(data);

        return Response.ok(response).build();
    }

    // ================== PAGINATION / STATS (impl des méthodes abstraites) ==================

    @Override
    @GET
    @Path("liste-users")
    public Response paginate(@QueryParam("size") int size, @QueryParam("page") int page) {
        int pageSize = size > 0 ? size : 20;
        int currentPage = page > 0 ? page : 1;

        // Récupérer les données paginées
        var query = accountRepository.findAll();
        Long total = query.count();
        
        List<AccountEntity> items = query
                .page(Page.of(currentPage - 1, pageSize))
                .list();

        // Construire la réponse au format demandé
        PaginatedResponse<AccountEntity> response = new PaginatedResponse<>();
        response.setStatusCode(7000);
        response.setStatusMessage("SUCCESS.");
        
        // Créer l'objet data
        PaginatedResponse.PaginatedData<AccountEntity> data = new PaginatedResponse.PaginatedData<>();
        data.setTotal(total);
        data.setPageSize(pageSize);
        data.setPage(currentPage);
        data.setItems(items);
        
        response.setData(data);

        return Response.ok(response).build();
    }

    @Override
    @GET
    @Path("paginate/trash")
    public Response paginateTrash(@QueryParam("size") int size, @QueryParam("page") int page) {
        int perPage = size > 0 ? size : 25;
        int currentPage = page > 0 ? page : 1;

        List<AccountEntity> items = accountRepository
                .find("deleted = true")
                .page(Page.of(currentPage - 1, perPage))
                .list();

        ApiResponse<List<AccountEntity>> resp = new ApiResponse<>();
        resp.setStatus_code(7000);
        resp.setStatus_message("Success");
        resp.setData(items);
        return Response.ok(resp).build();
    }

    @Override
    @GET
    @Path("stats")
    public Response stats() {
        long total = accountRepository.count();
        ApiResponse<Long> resp = new ApiResponse<>();
        resp.setStatus_code(7000);
        resp.setStatus_message("Success");
        resp.setData(total);
        return Response.ok(resp).build();
    }


    @POST
    @Path("register")
    @Transactional
    public Response register(RegisterRequest body) {
        ApiResponse<Object> response = new ApiResponse<>();

        // 1. Validation
        var violations = validator.validate(body);
        if (!violations.isEmpty()) {
            response.setStatus_code(400);
            response.setStatus_message("Erreur de validation");
            response.setData(violations);
            return Response.status(400).entity(response).build();
        }

        // 2. Vérifier si l'email existe déjà
        boolean emailExists = accountRepository
                .find("loginCmpt", body.getEmail())
                .firstResult() != null;
        if (emailExists) {
            response.setStatus_code(409);
            response.setStatus_message("Email déjà utilisé");
            return Response.status(409).entity(response).build();
        }

        // 3. Créer la Person
        PersonEntity person = new PersonEntity();
        person.setNomUser(body.getLastName());
        person.setPrenUser(body.getFirstName());
        person.setTelUser(body.getPhone());
        person.setEmailUser(body.getEmail());
        person.setGenreUser(body.getGender());
        person.setErrorSendConfirmationEmail((byte) 0);
        person.setAcceptationCgu((byte) 1);
        personRepository.persist(person);

        Long profilId = body.getProfileId();

        if (profilId == null) {
            ProfilUserEntity defaultProfil = profilUserRepository
                    .find("libProfil", "AGENT")  // ou un autre critère
                    .firstResult();
            profilId = defaultProfil != null ? defaultProfil.getId() : null;
        }
        // 4. Créer l'Account lié
        AccountEntity account = new AccountEntity();
        account.setIdUser(person.getId());           // attention: adapte au nom du getter ID
        account.setLoginCmpt(body.getEmail());
        // Hash du mot de passe avant stockage
        String hashedPassword = BcryptUtil.bcryptHash(body.getPassword());
        account.setMpCmpt(hashedPassword);
        account.setActive(true);
        account.setDeleted(false);
        account.setIdProfil(profilId);
        accountRepository.persist(account);

        // 5. Envoyer un email de bienvenue (asynchrone, ne bloque pas la réponse)
        try {
            emailService.sendWelcomeEmail(body.getEmail(), body.getFirstName());
        } catch (Exception e) {
            // Log l'erreur mais ne fait pas échouer l'inscription
            logger.error("Erreur lors de l'envoi de l'email de bienvenue: {}", e.getMessage(), e);
        }

        response.setStatus_code(7000);
        response.setStatus_message("Compte créé avec succès");
        response.setData(Map.of(
                "accountId", account.getId(),
                "personId", person.getId()
        ));

        return Response.ok(response).build();
    }

    /**
     * Endpoint pour admin : Créer un utilisateur et envoyer un email de validation
     */
    @POST
    @Path("create-user")
    @Transactional
    public Response createUser(CreateUserRequest body) {
        ApiResponse<Object> response = new ApiResponse<>();

        // 1. Validation
        var violations = validator.validate(body);
        if (!violations.isEmpty()) {
            response.setStatus_code(400);
            response.setStatus_message("Erreur de validation");
            response.setData(violations);
            return Response.status(400).entity(response).build();
        }

        // 2. Vérifier si l'email existe déjà
        boolean emailExists = accountRepository
                .find("loginCmpt", body.getEmail())
                .firstResult() != null;
        if (emailExists) {
            response.setStatus_code(409);
            response.setStatus_message("Email déjà utilisé");
            return Response.status(409).entity(response).build();
        }

        // 3. Créer la Person
        PersonEntity person = new PersonEntity();
        person.setNomUser(body.getLastName());
        person.setPrenUser(body.getFirstName());
        person.setTelUser(body.getPhone());
        person.setEmailUser(body.getEmail());
        person.setGenreUser(body.getGender());
        person.setErrorSendConfirmationEmail((byte) 0);
        person.setAcceptationCgu((byte) 0); // Pas encore accepté
        personRepository.persist(person);

        // 4. Déterminer le profil
        Long profilId = body.getProfileId();
        if (profilId == null) {
            ProfilUserEntity defaultProfil = profilUserRepository
                    .find("libProfil", "AGENT")
                    .firstResult();
            profilId = defaultProfil != null ? defaultProfil.getId() : null;
        }

        // 5. Générer un token unique pour la validation
        String validationToken = java.util.UUID.randomUUID().toString();

        // 6. Créer l'Account (INACTIF, avec mot de passe temporaire hashé)
        AccountEntity account = new AccountEntity();
        account.setIdUser(person.getId());
        account.setLoginCmpt(body.getEmail());  
        // Mot de passe temporaire sécurisé (hashé), remplacé lors de la validation
        String tempPassword = "TEMP_" + java.util.UUID.randomUUID().toString();
        String hashedTempPassword = BcryptUtil.bcryptHash(tempPassword);
        account.setMpCmpt(hashedTempPassword);
        account.setSessionToken(validationToken);  // Token stocké ici
        account.setActive(false);  // Compte inactif jusqu'à validation
        account.setDeleted(false);
        account.setIdProfil(profilId);
        accountRepository.persist(account);

        // 7. Envoyer l'email avec le lien de validation
        try {
            // TODO: Remplacer par l'URL de ton frontend
            String validationUrl = "http://localhost:8081/accounts/validate?token=" + validationToken;
            // Exemple avec frontend : "https://ton-frontend.com/validate-account?token=" + validationToken
            
            Map<String, String> templateData = new HashMap<>();
            templateData.put("name", body.getFirstName() + " " + body.getLastName());
            templateData.put("validationLink", validationUrl);
            
            String subject = "Activez votre compte e-Parapheur";
            boolean emailSent = emailService.send(validationTemplate, templateData, body.getEmail(), subject);
            
            if (!emailSent) {
                logger.warn("Échec de l'envoi de l'email de validation à {}", body.getEmail());
            } else {
                logger.info("Email de validation envoyé à {}", body.getEmail());
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email de validation: {}", e.getMessage(), e);
            // Ne pas faire échouer la création, mais logger l'erreur
        }

        response.setStatus_code(7000);
        response.setStatus_message("Utilisateur créé avec succès. Email de validation envoyé.");
        response.setData(Map.of(
                "accountId", account.getId(),
                "personId", person.getId()
        ));

        return Response.ok(response).build();
    }

    /**
     * Endpoint pour valider le compte et définir le mot de passe
     */
    @POST
    @Path("validate")
    @Transactional
    public Response validateAccount(ValidateAccountRequest body) {
        ApiResponse<Object> response = new ApiResponse<>();

        // 1. Validation
        var violations = validator.validate(body);
        if (!violations.isEmpty()) {
            response.setStatus_code(400);
            response.setStatus_message("Erreur de validation");
            response.setData(violations);
            return Response.status(400).entity(response).build();
        }

        // 2. Chercher le compte par token
        AccountEntity account = accountRepository
                .find("sessionToken", body.getToken())
                .firstResult();

        if (account == null) {
            response.setStatus_code(404);
            response.setStatus_message("Token invalide ou expiré");
            return Response.status(404).entity(response).build();
        }

        // 3. Vérifier si le compte n'est pas déjà activé
        if (Boolean.TRUE.equals(account.getActive())) {
            response.setStatus_code(409);
            response.setStatus_message("Ce compte est déjà activé");
            return Response.status(409).entity(response).build();
        }

        // 4. Définir le mot de passe (hashé) et activer le compte
        String hashed = BcryptUtil.bcryptHash(body.getPassword());
        account.setMpCmpt(hashed);
        account.setActive(true);  // Activer le compte
        account.setSessionToken(null);  // Supprimer le token (usage unique)
        accountRepository.persist(account);

        // 5. Mettre à jour la Person (accepter CGU)
        PersonEntity person = personRepository.findById(account.getIdUser());
        if (person != null) {
            person.setAcceptationCgu((byte) 1);
            personRepository.persist(person);
        }

        response.setStatus_code(7000);
        response.setStatus_message("Compte activé avec succès");
        response.setData(Map.of(
                "accountId", account.getId(),
                "message", "Vous pouvez maintenant vous connecter"
        ));

        return Response.ok(response).build();
    }

    private String generateToken(AccountEntity account) {
        Instant expired = Instant.now().plusSeconds(24 * 60 * 60); // 24h

        var builder = Jwt.upn(account.getLoginCmpt())
                .claim("accountId", account.getId())
                .claim("userId", account.getIdUser())
                .expiresAt(expired);

        // Ne mettre profileId que s'il n'est pas null
        if (account.getIdProfil() != null) {
            builder = builder.claim("profileId", account.getIdProfil());
        }

        return builder.sign();
    }

    // ... existing code ...

    /**
     * Route de test pour l'envoi d'email.
     * Exemple: GET /accounts/test-mail?to=jean@yopmail.com
     */
    @GET
    @Path("test-mail")
    public Response testMail(@QueryParam("to") String to) {
        ApiResponse<Object> response = new ApiResponse<>();

        if (to == null || to.isBlank()) {
            response.setStatus_code(400);
            response.setStatus_message("Paramètre 'to' obligatoire");
            return Response.status(400).entity(response).build();
        }

        try {
            String subject = "Test d'envoi d'email - e-Parapheur";
            String body = "Ceci est un email de test envoyé depuis e-Parapheur.";
            emailService.sendTextEmail(to, subject, body);

            response.setStatus_code(7000);
            response.setStatus_message("Email de test envoyé à " + to);
            return Response.ok(response).build();
        } catch (Exception e) {
            response.setStatus_code(500);
            response.setStatus_message("Erreur lors de l'envoi de l'email: " + e.getMessage());
            return Response.status(500).entity(response).build();
        }
    }


}
