package com.eparapheur.resources;

import com.eparapheur.core.CrudEndPointImpl;
import com.eparapheur.core.features.ApiResponse;
import com.eparapheur.core.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.eparapheur.core.services.EmailService;
import com.eparapheur.core.services.FileStorageService;
import com.eparapheur.core.services.OtpService;
import com.eparapheur.db.entities.*;
import com.eparapheur.db.repositories.*;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Validator;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/signature-programs")
@ApplicationScoped
public class SignatureProgramManager extends CrudEndPointImpl<SignatureProgramEntity> {
    
    private static final Logger logger = LoggerFactory.getLogger(SignatureProgramManager.class);
    
    // Constantes pour les statuts
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PENDING = "PENDING";
    private static final String ACTION_CREATE_PROGRAM = "CREAT_PROGRAM";
    
    @Inject
    SignatureProgramRepository programRepository;
    
    @Inject
    ProgramStepRepository stepRepository;
    
    @Inject
    StepParticipantRepository participantRepository;
    
    @Inject
    DocumentRepository documentRepository;
    
    @Inject
    AccountRepository accountRepository;
    
    @Inject
    OtpService otpService;
    
    @Inject
    EmailService emailService;
    
    @Inject
    FileStorageService fileStorageService;
    
    @Inject
    PersonRepository personRepository;
    
    @Inject
    EntityManager entityManager;
    
    @Inject
    Validator validator;
    
    @PostConstruct
    void init() {
        setT(programRepository);
    }
    
    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Operation(summary = "Créer un programme de signature", 
               description = "Crée un programme de signature avec validation OTP")
    public Response create(CreateSignatureProgramRequest request) {
        try {
            // Validation Bean Validation
            Response validationResponse = validateRequest(request);
            if (validationResponse != null) {
                return validationResponse;
            }
            
            // 1. Récupérer et valider le compte
            AccountEntity account = findAccountByEmail(request.getEmail());
            if (account == null) {
                return buildErrorResponse(404, "Aucun compte trouvé pour cet email", null);
            }
            
            // 2. Valider l'OTP
            OtpEntity otpEntity = otpService.verifyOtp(
                account.getId(), 
                request.getOtp(), 
                ACTION_CREATE_PROGRAM
            );
            
            if (otpEntity == null) {
                return buildErrorResponse(401, "Code OTP invalide ou expiré", null);
            }
            
            // 3. Valider les dates
            Response dateValidationResponse = validateDates(request);
            if (dateValidationResponse != null) {
                return dateValidationResponse;
            }
            
            // 4. Valider les index de documents
            Response documentIndexValidationResponse = validateDocumentIndexes(request);
            if (documentIndexValidationResponse != null) {
                return documentIndexValidationResponse;
            }
            
            // 5. Créer le programme
            SignatureProgramEntity program = createProgram(request, account);
            programRepository.persist(program);
            programRepository.flush(); // Nécessaire pour obtenir l'ID
            
            // 6. Créer les documents et mapper index -> documentId
            Map<Integer, Long> documentIndexToId = createDocuments(request.getDocuments(), account.getId());
            
            // 7. Créer les étapes, participants et associations documents
            Response stepsValidationResponse = validateParticipants(request);
            if (stepsValidationResponse != null) {
                return stepsValidationResponse;
            }
            
            createSteps(program, request.getSteps(), documentIndexToId);
            
            // 8. Marquer l'OTP comme utilisé
            otpService.markAsUsed(otpEntity);
            
            // 9. Retourner la réponse
            return buildSuccessResponse(program);
            
        } catch (Exception e) {
            logger.error("Erreur lors de la création du programme", e);
            return buildErrorResponse(500, "Erreur lors de la création du programme", null);
        }
    }
    
    /**
     * Valide la requête avec Bean Validation
     */
    private Response validateRequest(CreateSignatureProgramRequest request) {
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>();
            response.setStatus_code(400);
            response.setStatus_message("Erreur de validation");
            response.setData(Map.of("violations", violations));
            return Response.status(400).entity(response).build();
        }
        return null;
    }
    
    /**
     * Valide que les dates sont cohérentes
     */
    private Response validateDates(CreateSignatureProgramRequest request) {
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getEndDate().isBefore(request.getStartDate())) {
                return buildErrorResponse(400, "La date de fin doit être après la date de début", null);
            }
        }
        return null;
    }
    
    /**
     * Valide que les index de documents sont valides
     */
    private Response validateDocumentIndexes(CreateSignatureProgramRequest request) {
        int documentCount = request.getDocuments().size();
        for (CreateStepRequest stepReq : request.getSteps()) {
            for (Integer docIndex : stepReq.getDocumentIds()) {
                if (docIndex < 0 || docIndex >= documentCount) {
                    return buildErrorResponse(400, 
                        String.format("Index de document invalide: %d (doit être entre 0 et %d)", 
                            docIndex, documentCount - 1), null);
                }
            }
        }
        return null;
    }
    
    /**
     * Valide que tous les participants existent
     */
    private Response validateParticipants(CreateSignatureProgramRequest request) {
        for (CreateStepRequest stepReq : request.getSteps()) {
            for (ParticipantRequest partReq : stepReq.getParticipants()) {
                AccountEntity participantAccount = accountRepository.findById(partReq.getAccountId());
                if (participantAccount == null) {
                    return buildErrorResponse(404, 
                        String.format("Compte participant introuvable: %d", partReq.getAccountId()), null);
                }
            }
        }
        return null;
    }
    
    /**
     * Récupère le compte par email
     */
    private AccountEntity findAccountByEmail(String email) {
        return accountRepository.find("loginCmpt", email).firstResult();
    }
    
    /**
     * Crée l'entité programme
     */
    private SignatureProgramEntity createProgram(CreateSignatureProgramRequest request, AccountEntity account) {
        SignatureProgramEntity program = new SignatureProgramEntity();
        program.setTitle(request.getLabel());
        program.setDescription(request.getDescription());
        program.setProgramType(request.getProgramType());
        program.setIdInitiatorAccount(account.getId());
        program.setStatus(STATUS_DRAFT);
        
        if (request.getStartDate() != null) {
            program.setStartDate(Timestamp.valueOf(
                request.getStartDate().atZone(ZoneId.systemDefault()).toLocalDateTime()
            ));
        }
        if (request.getEndDate() != null) {
            program.setEndDate(Timestamp.valueOf(
                request.getEndDate().atZone(ZoneId.systemDefault()).toLocalDateTime()
            ));
        }
        
        return program;
    }
    
    /**
     * Crée les documents et retourne un mapping index -> documentId
     * Utilise FileStorageService pour sauvegarder les fichiers binaires
     */
    private Map<Integer, Long> createDocuments(List<DocumentRequest> documents, Long accountId) {
        Map<Integer, Long> documentIndexToId = new HashMap<>();
        Timestamp uploadTime = new Timestamp(System.currentTimeMillis());
        
        for (int i = 0; i < documents.size(); i++) {
            DocumentRequest docReq = documents.get(i);
            
            // Sauvegarder le fichier binaire et obtenir le chemin
            String savedPath = fileStorageService.saveBase64File(
                docReq.getDocumentName(), 
                docReq.getBinary()
            );
            
            // Calculer la taille si non fournie
            Long documentSize = docReq.getDocumentSize();
            if (documentSize == null) {
                try {
                    byte[] fileBytes = java.util.Base64.getDecoder().decode(docReq.getBinary());
                    documentSize = (long) fileBytes.length;
                } catch (Exception e) {
                    logger.warn("Impossible de calculer la taille du document {}, utilisation de la valeur fournie", 
                        docReq.getDocumentName(), e);
                }
            }
            
            // Déduire le type MIME si non fourni
            String documentType = docReq.getDocumentType();
            if (documentType == null || documentType.isBlank()) {
                documentType = inferDocumentType(docReq.getDocumentName());
            }
            
            DocumentEntity doc = new DocumentEntity();
            doc.setDocumentName(docReq.getDocumentName());
            doc.setDocumentPath(savedPath); // Chemin généré par FileStorageService
            doc.setDocumentSize(documentSize);
            doc.setDocumentType(documentType);
            doc.setUploadedByAccount(accountId);
            doc.setUploadedAt(uploadTime);
            
            documentRepository.persist(doc);
            documentIndexToId.put(i, doc.getId());
        }
        
        // Un seul flush() après tous les documents
        documentRepository.flush();
        
        return documentIndexToId;
    }
    
    /**
     * Déduit le type MIME à partir de l'extension du fichier
     */
    private String inferDocumentType(String fileName) {
        if (fileName == null) {
            return "application/octet-stream";
        }
        
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerName.endsWith(".doc")) {
            return "application/msword";
        } else if (lowerName.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (lowerName.endsWith(".xls")) {
            return "application/vnd.ms-excel";
        } else if (lowerName.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerName.endsWith(".png")) {
            return "image/png";
        } else {
            return "application/octet-stream";
        }
    }
    
    /**
     * Crée les étapes, participants et associations documents
     * Envoie également les emails d'invitation aux participants
     */
    private void createSteps(SignatureProgramEntity program, List<CreateStepRequest> steps, 
                             Map<Integer, Long> documentIndexToId) {
        // Récupérer l'initiateur pour le nom dans l'email
        AccountEntity initiator = accountRepository.findById(program.getIdInitiatorAccount());
        String initiatorName = null;
        if (initiator != null) {
            PersonEntity initiatorPerson = personRepository.findById(initiator.getIdUser());
            if (initiatorPerson != null) {
                String firstName = initiatorPerson.getPrenUser() != null ? initiatorPerson.getPrenUser() : "";
                String lastName = initiatorPerson.getNomUser() != null ? initiatorPerson.getNomUser() : "";
                initiatorName = (firstName + " " + lastName).trim();
                if (initiatorName.isBlank()) {
                    initiatorName = null;
                }
            }
        }
        
        for (CreateStepRequest stepReq : steps) {
            // Créer l'étape
            ProgramStepEntity step = new ProgramStepEntity();
            step.setIdProgram(program.getId());
            step.setStepOrder(stepReq.getStepOrder());
            step.setName(stepReq.getName());
            step.setActionType(stepReq.getActionType());
            step.setDescription(stepReq.getDescription());
            step.setRequired(stepReq.getRequired() != null ? stepReq.getRequired() : true);
            step.setStatus(STATUS_PENDING);
            
            stepRepository.persist(step);
            stepRepository.flush(); // Nécessaire pour obtenir l'ID de l'étape
            
            // Associer les documents à l'étape
            for (Integer docIndex : stepReq.getDocumentIds()) {
                Long docId = documentIndexToId.get(docIndex);
                if (docId != null) {
                    StepDocumentEntity sd = new StepDocumentEntity();
                    sd.setIdStep(step.getId());
                    sd.setIdDocument(docId);
                    entityManager.persist(sd);
                }
            }
            
            // Récupérer les noms des documents de l'étape
            List<String> documentNames = new ArrayList<>();
            for (Integer docIndex : stepReq.getDocumentIds()) {
                Long docId = documentIndexToId.get(docIndex);
                if (docId != null) {
                    DocumentEntity doc = documentRepository.findById(docId);
                    if (doc != null && doc.getDocumentName() != null) {
                        documentNames.add(doc.getDocumentName());
                    }
                }
            }
            
            // Créer les participants et envoyer les emails d'invitation
            for (ParticipantRequest partReq : stepReq.getParticipants()) {
                StepParticipantEntity participant = new StepParticipantEntity();
                participant.setIdStep(step.getId());
                participant.setIdAccount(partReq.getAccountId());
                participant.setAction(stepReq.getActionType()); // Action de l'étape
                participant.setPosition(partReq.getPosition() != null ? partReq.getPosition() : 0);
                participant.setRequired(partReq.getRequired() != null ? partReq.getRequired() : true);
                participant.setStatus(STATUS_PENDING);
                
                participantRepository.persist(participant);
                
                // Récupérer les noms des autres participants (exclure le participant actuel)
                List<String> otherParticipantsNames = new ArrayList<>();
                for (ParticipantRequest otherPartReq : stepReq.getParticipants()) {
                    if (!otherPartReq.getAccountId().equals(partReq.getAccountId())) {
                        AccountEntity otherAccount = accountRepository.findById(otherPartReq.getAccountId());
                        if (otherAccount != null) {
                            PersonEntity otherPerson = personRepository.findById(otherAccount.getIdUser());
                            if (otherPerson != null) {
                                String firstName = otherPerson.getPrenUser() != null ? otherPerson.getPrenUser() : "";
                                String lastName = otherPerson.getNomUser() != null ? otherPerson.getNomUser() : "";
                                String fullName = (firstName + " " + lastName).trim();
                                if (!fullName.isBlank()) {
                                    otherParticipantsNames.add(fullName);
                                } else if (otherAccount.getLoginCmpt() != null) {
                                    otherParticipantsNames.add(otherAccount.getLoginCmpt());
                                }
                            }
                        }
                    }
                }
                
                // Envoyer l'email d'invitation au participant
                try {
                    AccountEntity participantAccount = accountRepository.findById(partReq.getAccountId());
                    if (participantAccount != null && participantAccount.getLoginCmpt() != null) {
                        // Récupérer le nom du participant depuis PersonEntity
                        String participantName = null;
                        PersonEntity participantPerson = personRepository.findById(participantAccount.getIdUser());
                        if (participantPerson != null) {
                            String firstName = participantPerson.getPrenUser() != null ? participantPerson.getPrenUser() : "";
                            String lastName = participantPerson.getNomUser() != null ? participantPerson.getNomUser() : "";
                            participantName = (firstName + " " + lastName).trim();
                            if (participantName.isBlank()) {
                                participantName = null;
                            }
                        }
                        
                        // Construire l'URL du programme (à adapter selon votre frontend)
                        String programUrl = String.format("/signature-programs/%d", program.getId());
                        
                        emailService.sendSignatureInvitation(
                            participantAccount.getLoginCmpt(), // Email
                            participantName,
                            program.getTitle(),
                            program.getDescription(),
                            step.getName(),
                            stepReq.getActionType(),
                            initiatorName,
                            programUrl,
                            documentNames,  // Liste des documents
                            otherParticipantsNames  // Liste des autres participants
                        );
                        
                        logger.info("Email d'invitation envoyé à: {} pour le programme: {}", 
                            participantAccount.getLoginCmpt(), program.getTitle());
                    }
                } catch (Exception e) {
                    // Log l'erreur mais ne fait pas échouer la création du programme
                    logger.error("Erreur lors de l'envoi de l'email d'invitation au participant {}: {}", 
                        partReq.getAccountId(), e.getMessage(), e);
                }
            }
        }
    }
    
    /**
     * Construit une réponse de succès
     */
    private Response buildSuccessResponse(SignatureProgramEntity program) {
        ApiResponse<Map<String, Object>> response = new ApiResponse<>();
        Map<String, Object> data = new HashMap<>();
        data.put("programId", program.getId());
        data.put("title", program.getTitle());
        data.put("status", program.getStatus());
        
        response.setStatus_code(HttpContextStatus.SUCCESS_OPERATION);
        response.setStatus_message("Programme de signature créé avec succès");
        response.setData(data);
        
        return Response.ok(response).build();
    }
    
    /**
     * Construit une réponse d'erreur
     */
    private Response buildErrorResponse(int statusCode, String message, Object data) {
        ApiResponse<Object> response = new ApiResponse<>();
        response.setStatus_code(statusCode);
        response.setStatus_message(message);
        response.setData(data);
        return Response.status(statusCode).entity(response).build();
    }
    

    @Override
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Operation(summary = "Obtenir un programme de signature par ID",
               description = "Retourne les détails d'un programme de signature")
    public Response get(@PathParam("id") Long id) throws JsonProcessingException {
        ApiResponse<SignatureProgramDTO> response = new ApiResponse<>();
        
        try {
            SignatureProgramEntity program = programRepository.findById(id);
            
            if (program == null) {
                response.setStatus_code(404);
                response.setStatus_message("Programme de signature non trouvé");
                response.setData(null);
                return Response.status(404).entity(response).build();
            }
            
            // Transformer l'entité en DTO pour éviter les références circulaires
            SignatureProgramDTO dto = mapToSignatureProgramDTO(program);
            
            response.setStatus_code(7000);
            response.setStatus_message("Success");
            response.setData(dto);
            
            return Response.ok(response).build();
            
        } catch (Exception ex) {
            logger.error("Erreur lors de la récupération du programme {}", id, ex);
            response.setStatus_code(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            response.setStatus_message("Erreur lors de la récupération du programme");
            response.setData(null);
            return Response.status(500).entity(response).build();
        }
    }
    

    @Override
    @GET
    @Path("liste-all")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Operation(summary = "Obtenir la liste paginée des programmes de signature",
            description = "Retourne la liste paginée des programmes de signature")
    public Response paginate(@QueryParam("size") int size, @QueryParam("page") int page) {
        PaginatedResponse<SignatureProgramDTO> response = new PaginatedResponse<>();
        try {
            int pageSize = size > 0 ? size : 25;
            int currentPage = page > 0 ? page : 1;

            // Créer une requête sans filtre deleted (car cette entité n'a pas ce champ)
            PanacheQuery<SignatureProgramEntity> query = programRepository.findAll();
            Long total = query.count();

            List<SignatureProgramEntity> firstPage = query.page(currentPage - 1, pageSize).list();

            // Transformer les entités en DTOs pour éviter les références circulaires
            List<SignatureProgramDTO> items = firstPage.stream()
                    .map(this::mapToSignatureProgramDTO)
                    .collect(Collectors.toList());

            // Construire la réponse au format PaginatedResponse
            response.setStatusCode(7000);
            response.setStatusMessage("SUCCESS.");

            // Créer l'objet data
            PaginatedResponse.PaginatedData<SignatureProgramDTO> data = new PaginatedResponse.PaginatedData<>();
            data.setTotal(total);
            data.setPageSize(pageSize);
            data.setPage(currentPage);
            data.setItems(items);

            response.setData(data);

        } catch (Exception ex) {
            logger.error("Erreur lors de la pagination des programmes", ex);
            response.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
            response.setStatusMessage(ex.getLocalizedMessage());
        }

        return Response.status(200).entity(response).build();
    }
    
    /**
     * Méthode utilitaire pour mapper un SignatureProgramEntity vers SignatureProgramDTO
     * Évite les références circulaires lors de la sérialisation JSON
     */
    private SignatureProgramDTO mapToSignatureProgramDTO(SignatureProgramEntity program) {
        SignatureProgramDTO dto = new SignatureProgramDTO();
        
        // Données de base du programme
        dto.setId(program.getId());
        dto.setTitle(program.getTitle());
        dto.setDescription(program.getDescription());
        dto.setIdInitiatorAccount(program.getIdInitiatorAccount());
        dto.setProgramType(program.getProgramType());
        dto.setStatus(program.getStatus());
        dto.setStartDate(program.getStartDate());
        dto.setEndDate(program.getEndDate());
        dto.setCreatedAt(program.getCreatedAt());
        dto.setUpdatedAt(program.getUpdatedAt());
        
        // Mapper l'initiateur
        if (program.getIdInitiatorAccount() != null) {
            AccountEntity initiatorAccount = accountRepository.findById(program.getIdInitiatorAccount());
            if (initiatorAccount != null) {
                AccountDetailDTO initiatorDTO = new AccountDetailDTO();
                initiatorDTO.setId(initiatorAccount.getId());
                initiatorDTO.setLogin(initiatorAccount.getLoginCmpt());
                initiatorDTO.setActive(initiatorAccount.getActive());
                initiatorDTO.setDeleted(initiatorAccount.getDeleted());
                initiatorDTO.setCreatedAt(initiatorAccount.getCreatedAt());
                initiatorDTO.setUpdatedAt(initiatorAccount.getUpdatedAt());
                
                // Charger les infos de la personne
                PersonEntity person = personRepository.findById(initiatorAccount.getIdUser());
                if (person != null) {
                    PersonDTO personDTO = new PersonDTO();
                    personDTO.setId(person.getId());
                    personDTO.setFirstName(person.getPrenUser());
                    personDTO.setLastName(person.getNomUser());
                    personDTO.setEmail(person.getEmailUser());
                    personDTO.setPhone(person.getTelUser());
                    personDTO.setGender(person.getGenreUser());
                    personDTO.setCode(person.getCodeUser());
                    initiatorDTO.setPerson(personDTO);
                }
                
                dto.setInitiator(initiatorDTO);
            }
        }
        
        // Mapper les étapes (sans référence circulaire)
        // Charger explicitement les steps pour éviter LazyInitializationException
        List<ProgramStepEntity> steps = stepRepository.find("idProgram", program.getId()).list();
        if (steps != null && !steps.isEmpty()) {
            List<ProgramStepDTO> stepDTOs = steps.stream()
                    .map(this::mapToProgramStepDTO)
                    .collect(Collectors.toList());
            dto.setSteps(stepDTOs);
        }
        
        return dto;
    }
    
    /**
     * Mapper une étape vers son DTO
     */
    private ProgramStepDTO mapToProgramStepDTO(ProgramStepEntity step) {
        ProgramStepDTO dto = new ProgramStepDTO();
        
        dto.setId(step.getId());
        dto.setIdProgram(step.getIdProgram());  // Juste l'ID, pas l'objet
        dto.setStepOrder(step.getStepOrder());
        dto.setActionType(step.getActionType());
        dto.setName(step.getName());
        dto.setDescription(step.getDescription());
        dto.setRequired(step.getRequired());
        dto.setStatus(step.getStatus());
        dto.setCreatedAt(step.getCreatedAt());
        
        // Mapper les participants (charger explicitement)
        List<StepParticipantEntity> participants = participantRepository.find("idStep", step.getId()).list();
        if (participants != null && !participants.isEmpty()) {
            List<StepParticipantDTO> participantDTOs = participants.stream()
                    .map(this::mapToStepParticipantDTO)
                    .collect(Collectors.toList());
            dto.setParticipants(participantDTOs);
        }
        
        // Mapper les documents (charger explicitement via la table de jointure)
        // Les documents sont dans une relation ManyToMany, on doit les charger via StepDocumentEntity
        List<StepDocumentEntity> stepDocuments = entityManager
                .createQuery("SELECT sd FROM StepDocumentEntity sd WHERE sd.idStep = :stepId", StepDocumentEntity.class)
                .setParameter("stepId", step.getId())
                .getResultList();
        
        if (stepDocuments != null && !stepDocuments.isEmpty()) {
            List<DocumentDTO> documentDTOs = stepDocuments.stream()
                    .map(sd -> {
                        DocumentEntity doc = documentRepository.findById(sd.getIdDocument());
                        return doc != null ? mapToDocumentDTO(doc) : null;
                    })
                    .filter(doc -> doc != null)
                    .collect(Collectors.toList());
            dto.setDocuments(documentDTOs);
        }
        
        return dto;
    }
    
    /**
     * Mapper un participant vers son DTO
     */
    private StepParticipantDTO mapToStepParticipantDTO(StepParticipantEntity participant) {
        StepParticipantDTO dto = new StepParticipantDTO();
        
        dto.setId(participant.getId());
        dto.setIdStep(participant.getIdStep());
        dto.setIdAccount(participant.getIdAccount());
        dto.setAction(participant.getAction());
        dto.setPosition(participant.getPosition());
        dto.setRequired(participant.getRequired());
        dto.setStatus(participant.getStatus());
        dto.setCreatedAt(participant.getCreatedAt());
        
        // Optionnel : charger les infos du compte
        if (participant.getIdAccount() != null) {
            AccountEntity account = accountRepository.findById(participant.getIdAccount());
            if (account != null) {
                AccountDetailDTO accountDTO = new AccountDetailDTO();
                accountDTO.setId(account.getId());
                accountDTO.setLogin(account.getLoginCmpt());
                accountDTO.setActive(account.getActive());
                accountDTO.setDeleted(account.getDeleted());
                accountDTO.setCreatedAt(account.getCreatedAt());
                accountDTO.setUpdatedAt(account.getUpdatedAt());
                
                PersonEntity person = personRepository.findById(account.getIdUser());
                if (person != null) {
                    PersonDTO personDTO = new PersonDTO();
                    personDTO.setId(person.getId());
                    personDTO.setFirstName(person.getPrenUser());
                    personDTO.setLastName(person.getNomUser());
                    personDTO.setEmail(person.getEmailUser());
                    personDTO.setPhone(person.getTelUser());
                    personDTO.setGender(person.getGenreUser());
                    personDTO.setCode(person.getCodeUser());
                    accountDTO.setPerson(personDTO);
                }
                
                dto.setAccount(accountDTO);
            }
        }
        
        return dto;
    }
    
    /**
     * Mapper un document vers son DTO
     */
    private DocumentDTO mapToDocumentDTO(DocumentEntity document) {
        DocumentDTO dto = new DocumentDTO();
        
        dto.setId(document.getId());
        dto.setDocumentName(document.getDocumentName());
        dto.setDocumentPath(document.getDocumentPath());
        dto.setDocumentSize(document.getDocumentSize());
        dto.setDocumentType(document.getDocumentType());
        dto.setUploadedByAccount(document.getUploadedByAccount());
        dto.setUploadedAt(document.getUploadedAt());
        dto.setCreatedAt(document.getCreatedAt());
        
        return dto;
    }
}

