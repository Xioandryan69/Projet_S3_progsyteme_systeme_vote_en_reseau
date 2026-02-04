@echo off

echo "=== Compilation du client de vote ==="

REM Compiler les fichiers Java
javac -cp "lib\gson.jar;." -d bin src\main\*.java src\reseaux\*.java src\model\*.java src\data\*.java src\client\*.java

if %errorlevel% neq 0 (
    echo "Erreur lors de la compilation"
    exit /b 1
)

echo "=== Compilation réussie ==="
echo "=== Démarrage du client ==="

REM Lancer le client
REM Utiliser localhost par défaut, ou un argument en ligne de commande
set SERVER_IP=%1
set SERVER_PORT=%2

java -cp "lib\gson.jar;bin;." client.VoteClient %SERVER_IP% %SERVER_PORT%