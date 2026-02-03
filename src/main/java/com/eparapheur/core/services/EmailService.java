package com.eparapheur.core.services;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Inject
    Mailer mailer;

    @Inject
    @io.quarkus.qute.Location("otp")
    Template otpTemplate;

    @Inject
    @io.quarkus.qute.Location("signature-invitation")
    Template signatureInvitationTemplate;

    /**
     * Méthode générique pour envoyer un email avec un template Qute
     * @param template Template Qute injecté
     * @param data Données à injecter dans le template (Map<String, String>)
     * @param recipient Adresse email du destinataire
     * @param subject Sujet de l'email
     * @return true si l'email a été envoyé avec succès, false sinon
     */
    public boolean send(Template template, Map<String, String> data, String recipient, String subject) {
        try {
            // Créer une instance du template
            var templateInstance = template.instance();

            // Ajouter dynamiquement toutes les données du map au template
            for (Map.Entry<String, String> entry : data.entrySet()) {
                templateInstance.data(entry.getKey(), entry.getValue());
            }

            // Rendre le template avec toutes les données
            String htmlBody = templateInstance.render();

            // Optionnel : Ajouter un logo inline si disponible
            InputStream logoStream = getClass().getClassLoader().getResourceAsStream("static/logo.png");
            Mail mail;
            
            if (logoStream != null) {
                try {
                    byte[] logoBytes = logoStream.readAllBytes();
                    logoStream.close();
                    mail = Mail.withHtml(recipient, subject, htmlBody)
                            .addInlineAttachment("logo.png", logoBytes, "image/png", "logo-image");
                } catch (Exception e) {
                    logger.warn("Erreur lors de la lecture du logo, envoi sans logo: {}", e.getMessage());
                    mail = Mail.withHtml(recipient, subject, htmlBody);
                }
            } else {
                mail = Mail.withHtml(recipient, subject, htmlBody);
            }

            mailer.send(mail);
            logger.info("Email envoyé avec succès à {}: {}", recipient, subject);
            return true;
        } catch (Exception e) {
            logger.error("Échec de l'envoi de l'email à {}: {}", recipient, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Envoie un email en texte brut
     * @param to Adresse email du destinataire
     * @param subject Sujet de l'email
     * @param body Corps de l'email (texte)
     */
    public void sendTextEmail(String to, String subject, String body) {
        try {
            logger.info("Tentative d'envoi à: {} (sujet: {})", to, subject);
            mailer.send(Mail.withText(to, subject, body));
            logger.info("Email envoyé avec succès à: {}", to);
        } catch (Exception e) {
            logger.error("ERREUR SMTP lors de l'envoi à {}: {}", to, e.getMessage(), e);
            // Re-lancer pour voir l'erreur
            throw e;
        }
    }

    /**
     * Envoie un email en HTML
     * @param to Adresse email du destinataire
     * @param subject Sujet de l'email
     * @param htmlBody Corps de l'email (HTML)
     */
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        mailer.send(
            Mail.withHtml(to, subject, htmlBody)
        );
    }

    /**
     * Envoie un email avec texte et HTML
     * @param to Adresse email du destinataire
     * @param subject Sujet de l'email
     * @param textBody Corps de l'email (texte)
     * @param htmlBody Corps de l'email (HTML)
     */
    public void sendEmail(String to, String subject, String textBody, String htmlBody) {
        mailer.send(
            Mail.withText(to, subject, textBody)
                .setHtml(htmlBody)
        );
    }

    /**
     * Envoie un code OTP par email en utilisant le template Qute
     * @param to Adresse email du destinataire
     * @param otpCode Code OTP à envoyer
     * @param name Nom de l'utilisateur (optionnel)
     */
    public void sendOtpEmail(String to, String otpCode, String name) {
        try {
            String subject = "Code de vérification - e-Parapheur";
            
            // Préparer les données pour le template
            Map<String, String> templateData = new HashMap<>();
            templateData.put("otpCode", otpCode);
            if (name != null && !name.isBlank()) {
                templateData.put("name", name);
            }
            
            // Utiliser la méthode send() générique avec le template
            send(otpTemplate, templateData, to, subject);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email OTP: {}", e.getMessage(), e);
            // Fallback en cas d'erreur
            String textBody = "Votre code de vérification est : " + otpCode;
            sendEmail(to, "Code de vérification - e-Parapheur", textBody, null);
        }
    }

    /**
     * Envoie un code OTP par email (surcharge sans nom)
     * @param to Adresse email du destinataire
     * @param otpCode Code OTP à envoyer
     */
    public void sendOtpEmail(String to, String otpCode) {
        sendOtpEmail(to, otpCode, null);
    }

    /**
     * Envoie un email d'invitation à la signature
     * @param participantEmail Email du participant
     * @param participantName Nom du participant (optionnel)
     * @param programTitle Titre du programme
     * @param programDescription Description du programme (optionnel)
     * @param stepName Nom de l'étape (optionnel)
     * @param actionType Type d'action (SIGN, VALIDATION, PARAPHER)
     * @param initiatorName Nom de l'initiateur (optionnel)
     * @param programUrl URL pour accéder au programme (optionnel)
     * @param documentsList Liste des noms de documents (optionnel)
     * @param otherParticipantsList Liste des noms des autres participants (optionnel)
     */
    public void sendSignatureInvitation(
            String participantEmail,
            String participantName,
            String programTitle,
            String programDescription,
            String stepName,
            String actionType,
            String initiatorName,
            String programUrl,
            List<String> documentsList,
            List<String> otherParticipantsList) {
        try {
            String subject = "Invitation à la signature - " + programTitle;
            
            // Préparer les données pour le template (utiliser Object pour supporter les listes)
            Map<String, Object> templateData = new HashMap<>();
            if (participantName != null && !participantName.isBlank()) {
                templateData.put("participantName", participantName);
            }
            templateData.put("programTitle", programTitle);
            if (programDescription != null && !programDescription.isBlank()) {
                templateData.put("programDescription", programDescription);
            }
            if (stepName != null && !stepName.isBlank()) {
                templateData.put("stepName", stepName);
            }
            templateData.put("actionType", actionType);
            
            // Traduire le type d'action en français (verbe à l'infinitif)
            String actionTypeLabel = switch (actionType.toUpperCase()) {
                case "SIGN" -> "signer";
                case "VALIDATION" -> "valider";
                case "PARAPHER" -> "parapher";
                default -> actionType.toLowerCase();
            };
            templateData.put("actionTypeLabel", actionTypeLabel);
            
            if (initiatorName != null && !initiatorName.isBlank()) {
                templateData.put("initiatorName", initiatorName);
            }
            if (programUrl != null && !programUrl.isBlank()) {
                templateData.put("programUrl", programUrl);
            }
            
            // Ajouter les documents (toujours ajouter la clé, même si vide)
            if (documentsList != null && !documentsList.isEmpty()) {
                templateData.put("documentsList", documentsList);
                templateData.put("documentsCount", documentsList.size());
            } else {
                templateData.put("documentsList", new ArrayList<>());
                templateData.put("documentsCount", 0);
            }
            
            // Ajouter les autres participants (toujours ajouter la clé, même si vide)
            if (otherParticipantsList != null && !otherParticipantsList.isEmpty()) {
                templateData.put("otherParticipantsList", otherParticipantsList);
                templateData.put("otherParticipantsCount", otherParticipantsList.size());
            } else {
                templateData.put("otherParticipantsList", new ArrayList<>());
                templateData.put("otherParticipantsCount", 0);
            }
            
            // Créer une instance du template
            var templateInstance = signatureInvitationTemplate.instance();
            
            // Ajouter dynamiquement toutes les données au template
            for (Map.Entry<String, Object> entry : templateData.entrySet()) {
                templateInstance.data(entry.getKey(), entry.getValue());
            }
            
            // Rendre le template avec toutes les données
            String htmlBody = templateInstance.render();
            
            // Optionnel : Ajouter un logo inline si disponible
            InputStream logoStream = getClass().getClassLoader().getResourceAsStream("static/logo.png");
            Mail mail;
            
            if (logoStream != null) {
                try {
                    byte[] logoBytes = logoStream.readAllBytes();
                    logoStream.close();
                    mail = Mail.withHtml(participantEmail, subject, htmlBody)
                            .addInlineAttachment("logo.png", logoBytes, "image/png", "logo-image");
                } catch (Exception e) {
                    logger.warn("Erreur lors de la lecture du logo, envoi sans logo: {}", e.getMessage());
                    mail = Mail.withHtml(participantEmail, subject, htmlBody);
                }
            } else {
                mail = Mail.withHtml(participantEmail, subject, htmlBody);
            }
            
            mailer.send(mail);
            logger.info("Email d'invitation à la signature envoyé à: {}", participantEmail);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email d'invitation à {}: {}", 
                participantEmail, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email d'invitation", e);
        }
    }

    /**
     * Envoie un email de confirmation d'inscription
     * @param to Adresse email du destinataire
     * @param firstName Prénom de l'utilisateur
     */
    public void sendWelcomeEmail(String to, String firstName) {
        String subject = "Bienvenue sur e-Parapheur";
        String htmlBody = buildWelcomeEmailBody(firstName);
        String textBody = "Bonjour " + firstName + ",\n\nBienvenue sur e-Parapheur !";
        
        sendEmail(to, subject, textBody, htmlBody);
    }


    /**
     * Construit le corps HTML de l'email de bienvenue
     */
    private String buildWelcomeEmailBody(String firstName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Bienvenue sur e-Parapheur</h1>
                    </div>
                    <div class="content">
                        <h2>Bonjour %s,</h2>
                        <p>Votre compte a été créé avec succès !</p>
                        <p>Vous pouvez maintenant vous connecter et utiliser tous les services de la plateforme.</p>
                        <p>Merci de nous faire confiance.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2025 e-Parapheur. Tous droits réservés.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(firstName);
    }
}

