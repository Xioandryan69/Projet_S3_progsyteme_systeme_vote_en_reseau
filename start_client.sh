#!/bin/bash

# Script pour lancer un client interactif

echo "═════════════════════════════════════════════════════════════"
echo "       👤 CLIENT DE VOTE - SYSTÈME TCP"
echo "═════════════════════════════════════════════════════════════"

cd "$(dirname "$0")" || exit

SERVER_IP=${1:-localhost}
SERVER_PORT=${2:-5000}

echo ""
echo "📍 Serveur: $SERVER_IP:$SERVER_PORT"
echo ""
echo "📋 ÉLECTEURS DISPONIBLES:"
echo "   Code: ABC123 (E001)"
echo "   Code: XYZ789 (E002)"
echo "   Code: LMN456 (E003)"
echo ""
echo "🗳️  CANDIDATS DISPONIBLES:"
echo "   ID 1: Alice RAKOTO"
echo "   ID 2: Jean RABE"
echo "   ID 3: Marie ANDRY"
echo ""
echo "═════════════════════════════════════════════════════════════"
echo ""

# Lancer le client
java -cp "lib/gson.jar:bin:." client.VoteClient "$SERVER_IP" "$SERVER_PORT"
