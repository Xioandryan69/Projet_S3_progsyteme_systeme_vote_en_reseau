# 🧪 Guide de Test - Système de Vote en Réseau TCP

## 📊 Données de test

### Électeurs disponibles (src/data/electeurs.json)
```
E001 : Code = ABC123
E002 : Code = XYZ789
E003 : Code = LMN456
```

### Candidats disponibles (src/data/user.json)
```
ID 1 : Alice RAKOTO
ID 2 : Jean RABE
ID 3 : Marie ANDRY
```

---

## 🚀 Méthode 1 : Démonstration Complète (RECOMMANDÉE)

Lance le serveur + 3 clients de test automatiquement.

```bash
bash demo.sh
```

**Ce que ça fait :**
1. ✅ Compile tous les fichiers
2. ✅ Lance le serveur
3. ✅ 3 clients votent automatiquement
4. ✅ Affiche les résultats
5. ⏸️  Garde le serveur actif pour plus de tests

---

## 🖥️ Méthode 2 : Serveur + Clients Manuels

### Terminal 1 - Lancer le serveur
```bash
bash start_server.sh
```

**Sortie attendue :**
```
═════════════════════════════════════════════════════════════
       🚀 SERVEUR DE VOTE - SYSTÈME TCP
═════════════════════════════════════════════════════════════

📦 Compilation en cours...
✅ Compilation réussie

🖥️  Démarrage du serveur sur le port 5000...
   Appuyez sur Ctrl+C pour arrêter

Données chargées
Serveur de vote démarré sur le port 5000
```

### Terminal 2 - Lancer des clients
```bash
bash start_client.sh
```

**Interaction :**
```
═════════════════════════════════════════════════════════════
       👤 CLIENT DE VOTE - SYSTÈME TCP
═════════════════════════════════════════════════════════════

📍 Serveur: localhost:5000

📋 ÉLECTEURS DISPONIBLES:
   Code: ABC123 (E001)
   Code: XYZ789 (E002)
   Code: LMN456 (E003)

🗳️  CANDIDATS DISPONIBLES:
   ID 1: Alice RAKOTO
   ID 2: Jean RABE
   ID 3: Marie ANDRY

═════════════════════════════════════════════════════════════

✓ Connecté au serveur: localhost:5000

SYSTÈME DE VOTE EN RÉSEAU LOCAL

Entrez votre code électeur: ABC123
✓ Authentification réussie!

───────────────────────────────────────────
1. Voter
2. Voir les candidats
3. Voir les résultats
4. Quitter
───────────────────────────────────────────
Choisir une option (1-4): 
```

**Actions possibles :**
- Taper `1` : Voter
- Taper `2` : Voir candidats
- Taper `3` : Voir résultats
- Taper `4` : Quitter

---

## 🔄 Méthode 3 : Test Multi-Clients Automatique

**Prérequis :** Le serveur doit déjà être lancé (Terminal 1)

### Terminal 3
```bash
bash test_multi_clients.sh
```

Ou avec IP/port personnalisés :
```bash
bash test_multi_clients.sh 192.168.1.10 5000 5
```

**Ce que ça fait :**
- Lance 5 clients simultanément (par défaut 3)
- Chaque client vote automatiquement
- Affiche un résumé des actions

**Sortie :**
```
═════════════════════════════════════════════════════════════
       🧪 TEST MULTI-CLIENTS - SYSTÈME DE VOTE
═════════════════════════════════════════════════════════════

🔍 Vérification de la connexion au serveur...
✅ Serveur trouvé!

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
👤 CLIENT #1 - Code: ABC123
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
✓ Connecté au serveur: localhost:5000
✓ Authentification réussie!
✓ Vote enregistré avec succès!

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
👤 CLIENT #2 - Code: XYZ789
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
✓ Connecté au serveur: localhost:5000
✓ Authentification réussie!
✓ Vote enregistré avec succès!

...
```

---

## 🔧 Cas de Test Détaillés

### Test 1 : Authentification réussie
```bash
Code électeur : ABC123
Menu : 3 (Voir résultats)
Menu : 4 (Quitter)

Résultat attendu : ✓ Authentification réussie!
```

### Test 2 : Voter avec succès
```bash
Code électeur : XYZ789
Menu : 1 (Voter)
ID candidat : 2 (Jean RABE)
Menu : 4 (Quitter)

Résultat attendu : ✓ Vote enregistré avec succès!
```

### Test 3 : Voir les candidats
```bash
Code électeur : LMN456
Menu : 2 (Voir candidats)
Menu : 4 (Quitter)

Résultat attendu :
┌─ CANDIDATS DISPONIBLES ─────────────────┐
│ ID: 1 - Alice RAKOTO
│ ID: 2 - Jean RABE
│ ID: 3 - Marie ANDRY
└─────────────────────────────────────────┘
```

### Test 4 : Double vote empêché
```bash
Code électeur : ABC123 (vote pour Alice)
Menu : 1 (Voter)
ID candidat : 1
Menu : 1 (Voter à nouveau)
ID candidat : 2

Résultat attendu : ✗ Vous avez déjà voté
```

### Test 5 : Code invalide
```bash
Code électeur : INVALIDE
Menu : 1

Résultat attendu : ✗ Code électeur invalide
```

### Test 6 : Candidat invalide
```bash
Code électeur : ABC123
Menu : 1 (Voter)
ID candidat : 999

Résultat attendu : ✗ Candidat invalide
```

### Test 7 : Voir les résultats
```bash
Code électeur : ABC123
Menu : 3 (Voir résultats)

Résultat attendu :
┌─ RÉSULTATS DU VOTE ─────────────────────┐
│ Alice RAKOTO: X vote(s)
│ Jean RABE: Y vote(s)
│ Marie ANDRY: Z vote(s)
└─────────────────────────────────────────┘
```

---

## 📝 Scénario de Test Complet

**Durée :** ~5 minutes

### Étape 1 : Préparation (1 min)
```bash
# Terminal 1
bash start_server.sh
```

### Étape 2 : Test client 1 (1 min)
```bash
# Terminal 2
bash start_client.sh

# Entrer les données :
# Code : ABC123
# Menu : 2 (voir candidats)
# Menu : 1 (voter)
# ID : 1
# Menu : 4 (quitter)
```

### Étape 3 : Test client 2 (1 min)
```bash
# Terminal 2 (réutilisé)
bash start_client.sh

# Code : XYZ789
# Menu : 1 (voter)
# ID : 2
# Menu : 4 (quitter)
```

### Étape 4 : Test client 3 (1 min)
```bash
# Terminal 2 (réutilisé)
bash start_client.sh

# Code : LMN456
# Menu : 1 (voter)
# ID : 3
# Menu : 3 (résultats)
# Menu : 4 (quitter)
```

### Étape 5 : Vérifier (1 min)
```bash
# Terminal 2
bash start_client.sh

# Code : ABC123
# Menu : 3 (résultats)
# Menu : 4 (quitter)

# Vérifier que les 3 votes sont comptabilisés
```

---

## 📊 Fichiers de sortie

### Votes enregistrés
```
src/data/votes_records.txt
```

Chaque vote y est sauvegardé avec timestamp.

---

## 🔍 Dépannage

### Erreur : "Impossible de se connecter au serveur"
```bash
# Vérifier que le serveur est lancé
# Terminal 1 doit afficher "Serveur de vote démarré sur le port 5000"
```

### Erreur : "Code électeur invalide"
```bash
# Utiliser un code de src/data/electeurs.json
# ABC123, XYZ789, ou LMN456
```

### Erreur : "Port 5000 déjà utilisé"
```bash
# Arrêter le serveur précédent : pkill -f "ServeurVote"
# Ou lancer sur un autre port (modifier les scripts)
```

### Serveur ne compile pas
```bash
# Vérifier que gson.jar est présent
ls -la lib/gson.jar

# Vérifier que les fichiers JSON existent
ls -la src/data/*.json
```

---

## 🎯 Résumé des scripts

| Script | Utilité | Utilisation |
|--------|---------|-------------|
| `demo.sh` | Démonstration complète | `bash demo.sh` |
| `start_server.sh` | Serveur seul | `bash start_server.sh` |
| `start_client.sh` | Client interactif | `bash start_client.sh` |
| `test_multi_clients.sh` | Test multi-clients | `bash test_multi_clients.sh` |
| `run.sh` | (ancien) | À ne pas utiliser |

---

## ✅ Checklist de validation

- [ ] Serveur démarre sans erreur
- [ ] Client se connecte au serveur
- [ ] Authentification fonctionne
- [ ] Vote accepté pour code valide
- [ ] Double vote rejeté
- [ ] Résultats affichés correctement
- [ ] 3 clients votent simultanément
- [ ] Votes enregistrés en fichier
- [ ] Serveur gère les déconnexions

---

**Bon test ! 🎉**
