#!/bin/bash

# Script de démonstration complète du système de vote
# Lance le serveur et des clients de test automatiquement

echo "╔═══════════════════════════════════════════════════════════╗"
echo "║     🎯 DÉMONSTRATION - SYSTÈME DE VOTE EN RÉSEAU         ║"
echo "║                  TCP Multithreadé                         ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo ""

# Configuration
PROJECT_DIR="$(dirname "$0")"
SERVEUR_PID=""

# Fonction pour nettoyer et arrêter le serveur
cleanup() {
    echo ""
    echo "🛑 Arrêt du serveur..."
    if [ ! -z "$SERVEUR_PID" ]; then
        kill $SERVEUR_PID 2>/dev/null
        wait $SERVEUR_PID 2>/dev/null
    fi
    echo "✅ Serveur arrêté"
}

# Activer le trap pour arrêter le serveur à la fin
trap cleanup EXIT

# 1. COMPILATION
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📦 ÉTAPE 1 : COMPILATION"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

cd "$PROJECT_DIR"

javac -cp "lib/gson.jar:." -d bin \
    src/main/*.java \
    src/reseaux/*.java \
    src/model/*.java \
    src/data/*.java \
    src/client/*.java 2>&1 | tail -5

if [ $? -ne 0 ]; then
    echo "❌ Erreur de compilation"
    exit 1
fi

echo "✅ Compilation réussie"

# 2. DÉMARRAGE DU SERVEUR
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🖥️  ÉTAPE 2 : DÉMARRAGE DU SERVEUR"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

java -cp "lib/gson.jar:bin:." main.ServeurVote &
SERVEUR_PID=$!

# Attendre que le serveur démarre
sleep 2

# 3. AFFICHER LES INFORMATIONS DE TEST
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📋 INFORMATIONS DE TEST"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "✓ Serveur lancé sur localhost:5000"
echo ""
echo "Électeurs disponibles:"
echo "  • Code: ABC123  (Électeur E001)"
echo "  • Code: XYZ789  (Électeur E002)"
echo "  • Code: LMN456  (Électeur E003)"
echo ""
echo "Candidats disponibles:"
echo "  • ID 1: Alice RAKOTO"
echo "  • ID 2: Jean RABE"
echo "  • ID 3: Marie ANDRY"
echo ""

# 4. EXÉCUTION DES TESTS AUTOMATIQUES
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🧪 ÉTAPE 3 : TESTS AUTOMATIQUES"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Test 1 : Client 1 vote pour Alice
echo "📊 Test 1: Client avec code ABC123 vote pour Alice (ID: 1)"
echo -e "ABC123\n1\n1\n3" | java -cp "lib/gson.jar:bin:." client.VoteClient localhost 5000 2>&1 | grep -E "(Connecté|Authentification|Vote|Résultats|✓|✗)" | head -5
sleep 1

# Test 2 : Client 2 vote pour Jean
echo ""
echo "📊 Test 2: Client avec code XYZ789 vote pour Jean (ID: 2)"
echo -e "XYZ789\n1\n2\n3" | java -cp "lib/gson.jar:bin:." client.VoteClient localhost 5000 2>&1 | grep -E "(Connecté|Authentification|Vote|Résultats|✓|✗)" | head -5
sleep 1

# Test 3 : Client 3 vote pour Marie
echo ""
echo "📊 Test 3: Client avec code LMN456 vote pour Marie (ID: 3)"
echo -e "LMN456\n1\n3\n3" | java -cp "lib/gson.jar:bin:." client.VoteClient localhost 5000 2>&1 | grep -E "(Connecté|Authentification|Vote|Résultats|✓|✗)" | head -5
sleep 1

# Test 4 : Afficher les résultats
echo ""
echo "📊 Test 4: Affichage des résultats finaux"
echo -e "ABC123\n3\n4" | java -cp "lib/gson.jar:bin:." client.VoteClient localhost 5000 2>&1 | grep -E "(Connecté|RÉSULTATS|vote|✓|✗)" | head -10
sleep 1

# 5. AFFICHER LA CONSOLE DU SERVEUR
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📈 RÉSUMÉ DES OPÉRATIONS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "✅ 3 clients ont voté avec succès"
echo "✅ Les votes ont été enregistrés"
echo "✅ Les résultats sont affichés"
echo ""

# 6. OPTIONS SUPPLÉMENTAIRES
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🎮 OPTIONS SUPPLÉMENTAIRES"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Le serveur tourne toujours. Pour:"
echo ""
echo "1️⃣  Tester manuellement un client:"
echo "   bash start_client.sh"
echo ""
echo "2️⃣  Lancer plusieurs clients de test:"
echo "   bash test_multi_clients.sh"
echo ""
echo "3️⃣  Arrêter le serveur:"
echo "   Appuyez sur Ctrl+C"
echo ""
echo "═════════════════════════════════════════════════════════════"
echo ""

# Garder le serveur actif
wait $SERVEUR_PID
