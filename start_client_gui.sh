#!/bin/bash

# Lancer le client graphique

cd "$(dirname "$0")" || exit

# Compilation
CP="lib/gson.jar:lib/flatlaf-3.6.1.jar:."

javac -cp "$CP" -d bin \
    src/main/*.java \
    src/reseaux/*.java \
    src/model/*.java \
    src/data/*.java \
    src/client/*.java

if [ $? -ne 0 ]; then
    echo "Erreur lors de la compilation"
    exit 1
fi

# Démarrer le client GUI
java -cp "lib/gson.jar:lib/flatlaf-3.6.1.jar:bin:." client.VoteClientGUI
