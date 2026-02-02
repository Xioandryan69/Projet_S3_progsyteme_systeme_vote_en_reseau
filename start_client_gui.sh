#!/bin/bash

# Lancer le client graphique

cd "$(dirname "$0")" || exit

# Compilation
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

# Démarrer le client GUI
java -cp "lib/gson.jar:bin:." client.VoteClientGUI
