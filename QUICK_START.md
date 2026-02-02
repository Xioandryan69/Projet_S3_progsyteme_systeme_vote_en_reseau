# 🚀 GUIDE DE DÉMARRAGE RAPIDE

## ⚡ En 3 commandes

### Option A : Démo Automatique (Recommandé)
```bash
cd "Projet_S3_progsyteme_systeme_vote_en_reseau"
bash demo.sh
```
✅ Lance tout automatiquement - Serveur + 3 clients de test

---

### Option B : Serveur + Client Manuel

**Terminal 1 - Serveur :**
```bash
cd "Projet_S3_progsyteme_systeme_vote_en_reseau"
bash start_server.sh
```

**Terminal 2 - Client :**
```bash
cd "Projet_S3_progsyteme_systeme_vote_en_reseau"
bash start_client.sh
```

Données à utiliser :
- Code : `ABC123`, `XYZ789`, ou `LMN456`
- Candidat ID : `1`, `2`, ou `3`

---

### Option C : Multi-Clients Automatique

**Terminal 1 :**
```bash
bash start_server.sh
```

**Terminal 2 :**
```bash
bash test_multi_clients.sh
```

Cela lance 3 clients qui votent automatiquement

---

## 📋 Données disponibles

| Type | Valeur |
|------|--------|
| **Électeurs** | ABC123, XYZ789, LMN456 |
| **Candidats** | 1: Alice, 2: Jean, 3: Marie |
| **Port** | 5000 |
| **Serveur** | localhost |

---

## 📊 Résultats attendus

✅ 3 clients se connectent
✅ 3 votes enregistrés  
✅ Résultats affichés

---

## 🔗 Documentation complète

Voir `GUIDE_TEST.md` pour les détails complets

---

**Bon test ! 🎉**
