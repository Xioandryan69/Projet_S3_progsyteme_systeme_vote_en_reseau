package reseaux;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe pour traiter les requêtes des clients.
 * Décide quelle action effectuer selon la commande reçue.
 */
public class RequestProcessor 
{

    /**
     * Traiter une requête et retourner la réponse
     */
    public static String traiterRequete(Message message) {
        String commande = message.getCommand();

        if (commande.isEmpty()) {
            return buildMessage(Protocol.ERROR, "Message vide");
        }

        switch (commande) {
            // --- Vote classique ---
            case Protocol.LOGIN:
                return traiterLogin(message);
            case Protocol.GET_CANDIDATS:
                return traiterGetCandidats();
            case Protocol.VOTE:
                return traiterVote(message);
            case Protocol.GET_RESULTS:
                return traiterGetResults();

            // --- Sondages ---
            case Protocol.CREATE_POLL:
                return traiterCreatePoll(message);
            case Protocol.GET_POLLS:
                return traiterGetPolls();
            case Protocol.GET_OPTIONS:
                return traiterGetOptions(message);
            case Protocol.VOTE_POLL:
                return traiterVotePoll(message);
            case Protocol.GET_POLL_RESULTS:
                return traiterGetPollResults(message);

            default:
                return buildMessage(Protocol.ERROR, "Commande inconnue");
        }
    }

    // ------------------------------------------------------------
    // Méthodes pour le vote classique (candidats)
    // ------------------------------------------------------------

    /**
     * Traiter la commande LOGIN
     * Format: LOGIN|code_electeur
     */
    private static String traiterLogin(Message message) {
        if (message.getParamCount() < 1) {
            return buildMessage(Protocol.LOGIN_FAILED, "Paramètres manquants");
        }

        String code = message.getParam(1);

        if (!VoteService.authentifierUtilisateur(code)) {
            return buildMessage(Protocol.LOGIN_FAILED, Protocol.CODE_INVALIDE);
        }

        if (VoteService.aDejaVote(code)) {
            return buildMessage(Protocol.LOGIN_FAILED, Protocol.DEJA_VOTE);
        }

        return buildMessage(Protocol.LOGIN_SUCCESS, code);
    }

    /**
     * Traiter la commande GET_CANDIDATS
     */
    private static String traiterGetCandidats() 
    {
        String candidats = VoteService.obtenirListeCandidats();
        return buildMessage(Protocol.CANDIDATS_LIST, candidats);
    }

    /**
     * Traiter la commande VOTE
     * Format: VOTE|code_electeur|candidat_id
     */
    private static String traiterVote(Message message) {
        if (message.getParamCount() < 2) 
        {
            return buildMessage(Protocol.VOTE_REJECTED, "Paramètres manquants");
        }

        String code = message.getParam(1);
        String candidatIdStr = message.getParam(2);

        if (!VoteService.authentifierUtilisateur(code)) 
        {
            return buildMessage(Protocol.VOTE_REJECTED, Protocol.CODE_INVALIDE);
        }

        int candidatId;
        try {
            candidatId = Integer.parseInt(candidatIdStr);
        } catch (NumberFormatException e) {
            return buildMessage(Protocol.VOTE_REJECTED, Protocol.CANDIDAT_INVALIDE);
        }

        // Vérifier le statut de l'électeur
        if (!VoteService.isStatutValable(code)) {
            return buildMessage(Protocol.VOTE_REJECTED, Protocol.STATUT_INVALIDE);
        }

        
        if (!VoteService.candidatExiste(candidatId)) {
            return buildMessage(Protocol.VOTE_REJECTED, Protocol.CANDIDAT_INVALIDE);
        }

        if (VoteService.enregistrerVote(code, candidatId)) 
        {
            return buildMessage(Protocol.VOTE_ACCEPTED);
        } 
        
        else {
            return buildMessage(Protocol.VOTE_REJECTED, "Impossible d'enregistrer le vote");
        }
    }

    /**
     * Traiter la commande GET_RESULTS
     */
    private static String traiterGetResults() 
    {
        String resultats = VoteService.obtenirResultats();
        return buildMessage(Protocol.RESULTS, resultats);
    }


    /**
     * Traiter la commande CREATE_POLL
     * Format: CREATE_POLL|code_electeur|titre|option1|option2|...
     */
    private static String traiterCreatePoll(Message message) 
    {
        if (message.getParamCount() < 4) 
        {
            return buildMessage(Protocol.CREATE_POLL_FAILED, "Paramètres manquants");
        }
    
        String code = message.getParam(1);
    
        if (!VoteService.authentifierUtilisateur(code)) 
        {
            return buildMessage(Protocol.CREATE_POLL_FAILED, Protocol.CODE_INVALIDE);
        }
    
        String titre = message.getParam(2);
        String deadlineStr = message.getParam(3);
    
        java.time.LocalDateTime deadline;
    
        try {
            java.time.format.DateTimeFormatter formatter =
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            deadline = java.time.LocalDateTime.parse(deadlineStr, formatter);
        } 
        catch (Exception e) 
        {
            return buildMessage(Protocol.CREATE_POLL_FAILED, "Deadline invalide (format yyyy-MM-dd HH:mm)");
        }
    
        List<String> options = new ArrayList<>();
    
        for (int i = 4; i <= message.getParamCount(); i++) 
        {
            String opt = message.getParam(i);
            if (opt != null && !opt.trim().isEmpty()) 
            {
                options.add(opt.trim());
            }
        }
    
        if (options.isEmpty()) 
        {
            return buildMessage(Protocol.CREATE_POLL_FAILED, "Aucune option valide");
        }
    
        int pollId = PollService.creerPoll(titre, options, deadline);
    
        return buildMessage(Protocol.CREATE_POLL_SUCCESS, String.valueOf(pollId));
    }

    /**
     * Traiter la commande GET_POLLS
     * Format: GET_POLLS
     */
    private static String traiterGetPolls() {
        String liste = PollService.listerPolls();
        return buildMessage(Protocol.POLLS_LIST, liste);
    }

    /**
     * Traiter la commande GET_OPTIONS
     * Format: GET_OPTIONS|pollId
     */
    private static String traiterGetOptions(Message message) {
        if (message.getParamCount() < 1) {
            return buildMessage(Protocol.ERROR, "ID du sondage manquant");
        }

        String pollIdStr = message.getParam(1);
        int pollId;
        try {
            pollId = Integer.parseInt(pollIdStr);
        } catch (NumberFormatException e) {
            return buildMessage(Protocol.ERROR, "ID du sondage invalide");
        }

        String options = PollService.getOptions(pollId);
        if (options.isEmpty()) {
            return buildMessage(Protocol.ERROR, "Sondage inexistant ou sans options");
        }
        return buildMessage(Protocol.OPTIONS_LIST, options);
    }

    /**
     * Traiter la commande VOTE_POLL
     * Format: VOTE_POLL|code_electeur|pollId|optionId
     */
    private static String traiterVotePoll(Message message) {
        if (message.getParamCount() < 3) {
            return buildMessage(Protocol.VOTE_REJECTED, "Paramètres manquants");
        }

        String code = message.getParam(1);
        String pollIdStr = message.getParam(2);
        String optionIdStr = message.getParam(3);

        if (!VoteService.authentifierUtilisateur(code)) {
            return buildMessage(Protocol.VOTE_REJECTED, Protocol.CODE_INVALIDE);
        }

        int pollId;
        int optionId;
        try {
            pollId = Integer.parseInt(pollIdStr);
            optionId = Integer.parseInt(optionIdStr);
        } catch (NumberFormatException e) {
            return buildMessage(Protocol.VOTE_REJECTED, "Format invalide");
        }

        // Vérifier que le sondage existe et est ouvert (PollService.enregistrerVote le fait déjà)
        boolean success = PollService.enregistrerVote(code, pollId, optionId);
        if (success) {
            return buildMessage(Protocol.VOTE_ACCEPTED);
        } else {
            // PollService renvoie false dans plusieurs cas : électeur inexistant, déjà voté, option invalide, sondage fermé...
            return buildMessage(Protocol.VOTE_REJECTED, "Vote non accepté");
        }
    }

    /**
     * Traiter la commande GET_POLL_RESULTS
     * Format: GET_POLL_RESULTS|pollId
     */
    private static String traiterGetPollResults(Message message) {
        if (message.getParamCount() < 1) {
            return buildMessage(Protocol.ERROR, "ID du sondage manquant");
        }

        String pollIdStr = message.getParam(1);
        int pollId;
        try {
            pollId = Integer.parseInt(pollIdStr);
        } catch (NumberFormatException e) {
            return buildMessage(Protocol.ERROR, "ID du sondage invalide");
        }

        String resultats = PollService.getResultats(pollId);
        if (resultats.isEmpty()) {
            return buildMessage(Protocol.ERROR, "Sondage inexistant");
        }
        return buildMessage(Protocol.POLL_RESULTS, resultats);
    }

    // ------------------------------------------------------------
    // Méthode utilitaire de construction de message
    // ------------------------------------------------------------
    private static String buildMessage(String... parts) {
        return String.join(Protocol.DELIMITER, parts);
    }
}