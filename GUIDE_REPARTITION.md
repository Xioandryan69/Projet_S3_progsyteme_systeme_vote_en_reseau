# 📋 Guide de répartition du projet - Système de vote en réseau TCP

## Vue d'ensemble
Système de vote en réseau local utilisant **Java + TCP Sockets** (sans WebSocket).
- **Architecture** : Serveur multithreadé + Clients TCP
- **Durée** : 10 jours (4 phases)
- **Équipe** : Ivo, Valisoa, Ny Avo

---

## 📦 Phase 1 : Conception (Jour 1) ✅ COMPLÉTÉE

### Tâches réalisées :
- ✅ Protocole de communication défini
- ✅ Structure de projet créée
- ✅ Classes communes implémentées

### Fichiers clés :
- `src/reseaux/Protocol.java` - Constantes du protocole
- `src/reseaux/Message.java` - Parser de messages

**Format des messages** : `COMMANDE|param1|param2|...`

---

## 🖥️ Phase 2 : Implémentation du serveur (Jours 2-4)

### 2.1 Ivo - Serveur Socket 

**Tâche** : Créer `VoteServer.java`
- Ouvrir ServerSocket sur le port 5000
- Boucle d'acceptation de connexions
- Créer un `ClientHandler` par client

**Fichier** : `src/main/ServeurVote.java` ✅ FAIT
**Code clé** :
```java
ServerSocket serverSocket = new ServerSocket(5000);
while (true) {
    Socket client = serverSocket.accept();
    new Thread(new ClientHandler(client)).start();
}
```

---

### 2.2 Valisoa - Gestion des threads clients

**Tâche** : Implémenter `ClientHandler.java`
- Lire messages du client
- Déléguer à `RequestProcessor`
- Envoyer réponses

**Fichier** : `src/reseaux/ClientHandler.java` ✅ FAIT

**Code clé** :
```java
public class ClientHandler implements Runnable {
    @Override
    public void run() {
        BufferedReader in = new BufferedReader(...);
        PrintWriter out = new PrintWriter(...);
        
        while ((msg = in.readLine()) != null) {
            Message message = new Message(msg);
            String reponse = RequestProcessor.traiterRequete(message);
            out.println(reponse);
        }
    }
}
```

---

### 2.3 Ny Avo - Logique de vote

**Tâche** : Implémenter `VoteService.java` et `RequestProcessor.java`

**Fichier VoteService** : `src/reseaux/VoteService.java` ✅ FAIT
- `authentifierUtilisateur(code)` - Vérifier le code
- `aDejaVote(code)` - Empêcher double vote
- `enregistrerVote()` - Sauvegarder le vote
- `obtenirResultats()` - Calculer les résultats

**Fichier RequestProcessor** : `src/reseaux/RequestProcessor.java` ✅ FAIT
- Traiter commandes LOGIN, VOTE, GET_CANDIDATS, GET_RESULTS

---

### 2.4 Ivo - Comptage et diffusion

**Tâche** : Améliorer `VoteService.obtenirResultats()`
- Calculer votes en temps réel ✅ FAIT
- Formater réponse : `NOM_CANDIDAT:nb_votes|...`

**Code clé** :
```java
public static String obtenirResultats() {
    for (Candidat c : DataStore.candidats.values()) {
        long count = DataStore.votes.stream()
            .filter(v -> v.getCandidatId() == c.getId()).count();
    }
}
```

---

### 2.5 Valisoa - Gestion des sessions

**Tâche** : Implémenter `SessionManager.java`

**Fichier** : `src/reseaux/SessionManager.java` ✅ FAIT
- `ouvrirSession()` - Démarrer vote
- `fermerSession()` - Arrêter vote
- `estOuvert()` - Vérifier état

**Checkpoint Jour 4** :
- ✅ Serveur accepte connexions
- ✅ ClientHandler traite messages
- ✅ VoteService fonctionne
- ✅ SessionManager gère le cycle

---

## 🖱️ Phase 3 : Implémentation du client (Jours 5-7)

### 3.1 Valisoa - Connexion client

**Tâche** : Créer `VoteClient.java`
- Socket vers serveur
- Authentification
- Envoi/réception messages

**Fichier** : `src/client/VoteClient.java` ✅ FAIT

---

### 3.2 Ny Avo - Interface utilisateur

**Tâche** : Améliorer `VoteClient` avec menu interactif
- Afficher les candidats
- Traiter les votes
- Afficher les résultats

**Fonctionnalités** ✅ DÉJÀ IMPLÉMENTÉES :
```
Menu:
  1. Voter
  2. Voir les candidats
  3. Voir les résultats
  4. Quitter
```

---

### 3.3 Ivo - Interface d'administration

**Tâche** : Créer `AdminClient.java` (TODO)
- Visualiser les résultats en direct
- Exporter résultats (CSV/TXT)
- Ouvrir/fermer session
- Minuteur

**À créer** : `src/client/AdminClient.java`

---

## 🧪 Phase 4 : Tests (Jours 8-10)

### 4.1 Ny Avo - Tests unitaires

**À tester** :
- `VoteService` - Authentification, double vote
- `Protocol` - Parsing messages
- `Message` - Construction/parsing

---

### 4.2 Ivo - Tests de charge

**À faire** :
- Simuler 10+ clients simultanés
- Vérifier pas de doublons de vote
- Tester stabilité serveur

**Script** : Créer `src/test/MultiClientTest.java`

---

### 4.3 Tous - Validation finale

- Tout fonctionne ?
- Rapport de test
- Documentation complète

---

## 🚀 Comment démarrer

### Compilation
```bash
cd "Projet_S3_progsyteme_systeme_vote_en_reseau"
chmod +x run.sh run_client.sh
bash run.sh          # Terminal 1 - Serveur
bash run_client.sh   # Terminal 2 - Client
```

### Fichiers de données
- `src/data/electeurs.json` - Liste des électeurs
- `src/data/user.json` - Liste des candidats
- `src/data/votes_records.txt` - Historique votes

---

## 📊 Répartition des fichiers

| Personne  | Fichiers | État |
|-----------|----------|------|
| **Ivo**   | `ServeurVote.java`, `ResultBroadcaster.java`, `AdminClient.java` | ✅ Partiellement |
| **Valisoa** | `ClientHandler.java`, `SessionManager.java`, `VoteClient.java` | ✅ Complet |
| **Ny Avo** | `Protocol.java`, `Message.java`, `VoteService.java`, `RequestProcessor.java` | ✅ Complet |

---

## ✅ Checklist d'intégration

### Jour 2 (Fin)
- [ ] Serveur accepte connexions
- [ ] Logs affichent les connexions

### Jour 3 (Fin)
- [ ] ClientHandler lit messages
- [ ] Messages parsés correctement

### Jour 4 (Fin)
- [ ] Authentification fonctionne
- [ ] Double vote empêché
- [ ] Votes enregistrés en fichier

### Jour 5 (Fin)
- [ ] Client se connecte au serveur
- [ ] LOGIN réussit

### Jour 6 (Fin)
- [ ] Menu client fonctionne
- [ ] Vote possible
- [ ] Résultats affichés

### Jour 7 (Fin)
- [ ] AdminClient créé
- [ ] Export CSV/TXT fonctionne

### Jour 8-10
- [ ] Tests unitaires passent
- [ ] Tests de charge réussissent
- [ ] Validation finale

---

## 🔧 Commandes utiles

### Compilation complète
```bash
javac -cp "lib/gson.jar:." -d bin src/main/*.java src/reseaux/*.java \
      src/model/*.java src/data/*.java src/client/*.java
```

### Lancer serveur
```bash
java -cp "lib/gson.jar:bin:." main.ServeurVote
```

### Lancer client
```bash
java -cp "lib/gson.jar:bin:." client.VoteClient localhost 5000
```

### Simuler multiple clients
```bash
for i in {1..5}; do
    java -cp "lib/gson.jar:bin:." client.VoteClient localhost 5000 &
done
```

---

## 📝 Format du protocole

| Commande | Requête | Réponse | Exemple |
|----------|---------|---------|---------|
| LOGIN | `LOGIN\|CODE` | `LOGIN_SUCCESS\|CODE` | `LOGIN\|EL001` |
| VOTE | `VOTE\|CODE\|ID_CANDIDAT` | `VOTE_ACCEPTED` | `VOTE\|EL001\|2` |
| GET_CANDIDATS | `GET_CANDIDATS` | `CANDIDATS_LIST\|1:Alice\|2:Bob` | |
| GET_RESULTS | `GET_RESULTS` | `RESULTS\|Alice:3\|Bob:5` | |

---

## 💡 Points clés

1. **Multithreading** : ✅ Chaque client = 1 thread
2. **TCP Sockets** : ✅ Pas de WebSocket
3. **Partage de code** : Git branches par personne
4. **Synchronisation** : Réunions quotidiennes (15 min)
5. **Sauvegardes** : Votes sauvegardés en fichier

---

## 🎯 Objectifs

- ✅ Phase 1 : Conception
- ✅ Phase 2 : Serveur (code de base fourni)
- ✅ Phase 3 : Client (code de base fourni)
- ⏳ Phase 4 : Tests et validation

**Bon projet ! 🚀**
