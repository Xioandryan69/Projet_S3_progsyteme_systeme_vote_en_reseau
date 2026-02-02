# Projet_S3_progsyteme_systeme_vote_en_reseau

## Vote en ligne :: logique 

## Quoi ??
Un serveur TCP multithreadé en C qui :

  écoute sur une IP locale + port

  accepte plusieurs clients sur le même réseau local

  crée 1 thread par client

  permet à chaque client de voter

➡️ Aucun navigateur
➡️ Aucun WebSocket
➡️ Uniquement sockets TCP Berkeley

##  Architecture LAN 
[ Client 1 ] ─┐
[ Client 2 ] ─┼──> [ Serveur de vote ]
[ Client 3 ] ─┘

Tous les ordinateurs sont sur :

le même Wi-Fi

ou le même switch Ethernet


## Pourquoi c’est du LAN (et pas Internet) ?

Parce que :

le serveur écoute sur une IP privée

les clients se connectent via cette IP

Exemples d’IP LAN :

192.168.1.10

192.168.0.5

10.0.0.2

👉 Pas d’IP publique
👉 Pas de DNS
👉 Pas de HTTP
👉 Pas de WebSocket




## Comment le projet fonctionne (TECHNIQUEMENT)??


## SERVER (C – sockets TCP)
socket()
bind()
listen()

while (1) {
    client_socket = accept()
    pthread_create(&thread, NULL, handle_client, client_socket)
}

✔️ accept() → un client arrive
✔️ pthread_create() → 1 client = 1 thread



## CLIENT 
socket()
connect(server_ip, port)
send("VOTE:Alice")
recv(response)
close()

Le client :

se connecte à l’IP du serveur

envoie son vote

reçoit confirmation


## Exemple réel de test en réseau local
🖥️ Machine serveur

ip a
# IP trouvée : 192.168.1.10


gcc server.c -lpthread -o server
./server

## Machine cliente (autre PC du même Wi-Fi)


gcc client.c -o client
./client 192.168.1.10


## Multithreading : pourquoi c’est essentiel ?
❓ Problème sans threads

1 client bloque les autres

serveur lent

vote séquentiel

✅ Solution avec threads

plusieurs clients votent en même temps

chaque client a son canal privé

architecture claire