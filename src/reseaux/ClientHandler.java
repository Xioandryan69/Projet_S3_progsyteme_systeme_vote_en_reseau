package reseaux;

import java.io.*;
import java.net.Socket;
import data.DataStore;
import model.*;

public class ClientHandler implements Runnable 
{

    private Socket socket;
    private Electeur electeur;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() 
    {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true
            );

            String message;

            while ((message = in.readLine()) != null) {
                String[] parts = message.split(";");

                if (Protocol.LOGIN.equals(parts[0])) {
                    String code = parts[1];
                    electeur = DataStore.electeurs.get(code);

                    if (electeur == null) {
                        out.println("ERROR;CODE_INVALIDE");
                    } else if (electeur.isAVote()) {
                        out.println("ERROR;DEJA_VOTE");
                    } else {
                        out.println("OK;LOGIN");
                    }
                }
            }

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
