#!/bin/bash

# Script pour démarrer le serveur de vote en arrière-plan

echo "═════════════════════════════════════════════════════════════"
echo "       🚀 SERVEUR DE VOTE - SYSTÈME TCP"
echo "═════════════════════════════════════════════════════════════"

cd "$(dirname "$0")" || exit

# Compilation
echo ""
echo "📦 Compilation en cours..."
javac -cp "lib/gson.jar:." -d bin \
    src/main/*.java \
    src/reseaux/*.java \
    src/model/*.java \
    src/data/*.java \
    src/client/*.java 2>&1

if [ $? -ne 0 ]; then
    echo "❌ Erreur lors de la compilation"
    exit 1
fi

echo "✅ Compilation réussie"
echo ""
echo "🖥️  Démarrage du serveur sur le port 5000..."
echo "   Appuyez sur Ctrl+C pour arrêter"
echo ""

# Lancer le serveur
java -cp "lib/gson.jar:bin:." main.ServeurVote
