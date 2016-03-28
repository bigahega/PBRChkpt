package Server.Primary;

import Server.Shared.KeyValueStore;
import Server.Shared.Request;
import Server.Shared.Response;
import Server.Shared.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 08.03.2016.
 */
public class Primary extends Server {

    private List<String> backupServerList;

    public Primary(List<String> backupServerList) {

        this.backupServerList = backupServerList;
        this.keyValueStore = new KeyValueStore();
        try {
            this.listenerSocket = new ServerSocket(this.port);
            while(true) {
                Socket client = this.listenerSocket.accept();
                ServerWorker serverWorker = new ServerWorker(client);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void checkpoint() {

    }

    private class ServerWorker extends Thread {

        private Socket client;

        public ServerWorker(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try (ObjectInput input = new ObjectInputStream(this.client.getInputStream())) {
                while (true) {
                    Object incoming = input.readObject();
                    if (incoming instanceof Integer) {
                        if ((int) incoming == 0xDEADBABA)
                            break;
                    } else if (incoming instanceof Request) {
                        Request request = (Request) incoming;
                        String requestedKey = request.getKey();
                        String value = keyValueStore.get(requestedKey);
                        Response response = new Response(value);
                        ObjectOutput output = new ObjectOutputStream(this.client.getOutputStream());
                        output.writeObject(response);
                        output.close();
                    }
                }
                this.client.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }
}