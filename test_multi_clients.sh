#!/bin/bash

# Script pour tester avec plusieurs clients simultanés
# Utilise les données du fichier electeurs.json

echo "═════════════════════════════════════════════════════════════"
echo "       🧪 TEST MULTI-CLIENTS - SYSTÈME DE VOTE"
echo "═════════════════════════════════════════════════════════════"

# Configuration
SERVER_IP=${1:-localhost}
SERVER_PORT=${2:-5000}
NUM_CLIENTS=${3:-3}

cd "$(dirname "$0")" || exit

# Vérifier que le serveur est lancé
echo ""
echo "🔍 Vérification de la connexion au serveur..."
timeout 2 bash -c "echo > /dev/tcp/$SERVER_IP/$SERVER_PORT" 2>/dev/null

if [ $? -ne 0 ]; then
    echo "❌ Le serveur n'est pas accessible sur $SERVER_IP:$SERVER_PORT"
    echo "   Lancez d'abord: bash start_server.sh"
    exit 1
fi

echo "✅ Serveur trouvé!"
echo ""

# Données de test (codes électeurs du fichier electeurs.json)
CODES=(
    "ABC123"    # E001
    "XYZ789"    # E002
    "LMN456"    # E003
)

# Candidats disponibles (du fichier user.json)
# ID: 1 = Alice RAKOTO
# ID: 2 = Jean RABE
# ID: 3 = Marie ANDRY

CANDIDATS=(1 2 3)

# Fonction pour simuler un client
test_client() {
    local client_num=$1
    local code=$2
    local candidat=$3
    
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "👤 CLIENT #$client_num - Code: $code"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    
    # Lancer le client Java avec les entrées simulées
    echo -e "$code\n1\n$candidat\n4" | \
    java -cp "lib/gson.jar:bin:." client.VoteClient "$SERVER_IP" "$SERVER_PORT" 2>&1 | \
    grep -E "(Connecté|Authentification|Candidat|Vote|Résultats|✓|✗|Erreur|CANDIDATS|RÉSULTATS|Déconnexion)"
    
    echo ""
}

# Lancer les clients
echo ""
echo "🚀 Lancement de $NUM_CLIENTS client(s) de test..."
echo ""

for i in $(seq 1 $NUM_CLIENTS); do
    # Sélectionner un code et un candidat
    code_index=$(( (i - 1) % ${#CODES[@]} ))
    candidat_index=$(( (i - 1) % ${#CANDIDATS[@]} ))
    
    code=${CODES[$code_index]}
    candidat=${CANDIDATS[$candidat_index]}
    
    # Lancer le client en arrière-plan
    test_client $i "$code" "$candidat" &
    
    # Petit délai entre les clients
    sleep 1
done

# Attendre que tous les clients terminent
wait

echo ""
echo "═════════════════════════════════════════════════════════════"
echo "✅ Test terminé"
echo "═════════════════════════════════════════════════════════════"
echo ""
echo "💡 Pour voir les résultats finals, connectez-vous avec:"
echo "   bash run_client.sh $SERVER_IP $SERVER_PORT"
echo ""
