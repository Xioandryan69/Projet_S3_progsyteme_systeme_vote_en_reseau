#!/bin/bash

echo "=== Compilation du serveur de vote ==="

# Compiler tous les fichiers Java
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
echo "=== Démarrage du serveur ==="

# Lancer le serveur
java -cp "lib/gson.jar:bin:." main.ServeurVote