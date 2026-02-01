package main;

import java.net.ServerSocket;
import java.net.Socket;
import data.*;
import reseaux.ClientHandler;

public class ServeurVote
{
    public static void main(String[] args) {
        int port = 5000;

        DataStore.chargerDonnees();
        System.out.println("Données chargées");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serveur de vote démarré sur le port " + port);

            while (true) {
                Socket client = serverSocket.accept();
                new Thread(new ClientHandler(client)).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
