#!/bin/bash

echo "=== Compilation du client de vote ==="

# Compiler les fichiers Java
javac -cp "lib/gson.jar:." -d bin \
    src/main/*.java \
    src/reseaux/*.java \
    src/model/*.java \
    src/data/*.java \
    src/client/*.java

if [ $? -ne 0 ]; then
    echo "Erreur lors de la compilation"
    exit 1
fi

echo "=== Compilation réussie ==="
echo "=== Démarrage du client ==="

# Lancer le client
# Utiliser localhost par défaut, ou un argument en ligne de commande
SERVER_IP=${1:-localhost}
SERVER_PORT=${2:-5000}

java -cp "lib/gson.jar:bin:." client.VoteClient "$SERVER_IP" "$SERVER_PORT"
