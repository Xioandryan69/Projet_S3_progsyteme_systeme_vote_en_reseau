package reseaux;

public class Protocol {

    // Commandes Client -> Serveur
    public static final String LOGIN = "LOGIN";
    public static final String GET_CANDIDATS = "GET_CANDIDATS";
    public static final String VOTE = "VOTE";
    public static final String GET_RESULTS = "GET_RESULTS";
    public static final String LOGOUT = "LOGOUT";

    // Réponses Serveur -> Client
    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILED = "LOGIN_FAILED";
    public static final String CANDIDATS_LIST = "CANDIDATS_LIST";
    public static final String VOTE_ACCEPTED = "VOTE_ACCEPTED";
    public static final String VOTE_REJECTED = "VOTE_REJECTED";
    public static final String RESULTS = "RESULTS";
    public static final String ERROR = "ERROR";

    // Délimiteur pour les messages
    public static final String DELIMITER = "|";

    // Codes d'erreur
    public static final String CODE_INVALIDE = "CODE_INVALIDE";
    public static final String DEJA_VOTE = "DEJA_VOTE";
    public static final String CANDIDAT_INVALIDE = "CANDIDAT_INVALIDE";
    public static final String SESSION_FERMEE = "SESSION_FERMEE";
}
