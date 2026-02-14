package reseaux;

public class Protocol {

    // ------------------------------------------------------------
    // Commandes Client -> Serveur
    // ------------------------------------------------------------
    public static final String LOGIN          = "LOGIN";
    public static final String GET_CANDIDATS  = "GET_CANDIDATS";
    public static final String VOTE           = "VOTE";
    public static final String GET_RESULTS    = "GET_RESULTS";
    public static final String LOGOUT         = "LOGOUT";

    // --- Commandes pour les sondages ---
    public static final String CREATE_POLL        = "CREATE_POLL";
    public static final String GET_POLLS          = "GET_POLLS";
    public static final String GET_OPTIONS        = "GET_OPTIONS";
    public static final String VOTE_POLL          = "VOTE_POLL";
    public static final String GET_POLL_RESULTS   = "GET_POLL_RESULTS";

    // ------------------------------------------------------------
    // Réponses Serveur -> Client
    // ------------------------------------------------------------
    public static final String LOGIN_SUCCESS   = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILED    = "LOGIN_FAILED";
    public static final String CANDIDATS_LIST  = "CANDIDATS_LIST";
    public static final String VOTE_ACCEPTED   = "VOTE_ACCEPTED";
    public static final String VOTE_REJECTED   = "VOTE_REJECTED";
    public static final String RESULTS         = "RESULTS";
    public static final String ERROR           = "ERROR";

    // --- Réponses pour les sondages ---
    public static final String CREATE_POLL_SUCCESS = "CREATE_POLL_SUCCESS";
    public static final String CREATE_POLL_FAILED  = "CREATE_POLL_FAILED";
    public static final String POLLS_LIST          = "POLLS_LIST";
    public static final String OPTIONS_LIST        = "OPTIONS_LIST";
    public static final String POLL_RESULTS        = "POLL_RESULTS";

    // ------------------------------------------------------------
    // Délimiteur pour les messages
    // ------------------------------------------------------------
    public static final String DELIMITER = "|";

    // ------------------------------------------------------------
    // Codes d'erreur
    // ------------------------------------------------------------
    public static final String CODE_INVALIDE     = "CODE_INVALIDE";
    public static final String DEJA_VOTE         = "DEJA_VOTE";
    public static final String CANDIDAT_INVALIDE = "CANDIDAT_INVALIDE";
    public static final String SESSION_FERMEE    = "SESSION_FERMEE";

    // (Optionnel) codes d'erreur supplémentaires pour sondages
    public static final String POLL_INEXISTANT   = "POLL_INEXISTANT";
    public static final String OPTION_INVALIDE   = "OPTION_INVALIDE";
    public static final String POLL_FERME        = "POLL_FERME";
}