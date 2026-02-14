package reseaux;

/**
 */

    /**
     * Traiter une requête et retourner la réponse
     */
    public static String traiterRequete(Message message) {
        String commande = message.getCommand();

        if (commande.isEmpty()) {
            return buildMessage(Protocol.ERROR, "Message vide");
        }

        switch (commande) {
            case Protocol.LOGIN:
                return traiterLogin(message);
            case Protocol.GET_CANDIDATS:
                return traiterGetCandidats();
            case Protocol.VOTE:
                return traiterVote(message);
            case Protocol.GET_RESULTS:
                return traiterGetResults();

            default:
                return buildMessage(Protocol.ERROR, "Commande inconnue");
        }
    }

    /**
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
        String candidats = VoteService.obtenirListeCandidats();
        return buildMessage(Protocol.CANDIDATS_LIST, candidats);
    }

    /**
     */
    private static String traiterVote(Message message) {
            return buildMessage(Protocol.VOTE_REJECTED, "Paramètres manquants");
        }

        String code = message.getParam(1);
        String candidatIdStr = message.getParam(2);

        int candidatId;
        try {
            candidatId = Integer.parseInt(candidatIdStr);
        } catch (NumberFormatException e) {
            return buildMessage(Protocol.VOTE_REJECTED, Protocol.CANDIDAT_INVALIDE);
        }

        if (!VoteService.candidatExiste(candidatId)) {
            return buildMessage(Protocol.VOTE_REJECTED, Protocol.CANDIDAT_INVALIDE);
        }

            return buildMessage(Protocol.VOTE_ACCEPTED);
            return buildMessage(Protocol.VOTE_REJECTED, "Impossible d'enregistrer le vote");
        }
    }

    /**
     * Traiter la commande GET_RESULTS
     */
        String resultats = VoteService.obtenirResultats();
        return buildMessage(Protocol.RESULTS, resultats);
    }

    /**
     */
    private static String buildMessage(String... parts) {
        return String.join(Protocol.DELIMITER, parts);
    }
}