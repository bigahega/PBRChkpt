package Server.Backup;

import Server.Shared.KeyValueStore;
import Server.Shared.Request;
import Server.Shared.RequestType;
import Server.Shared.Server;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 08.03.2016.
 */
public class Backup extends Server {

    private String primaryHost;

    public Backup(String primaryHost) {
        this.primaryHost = primaryHost;
        this.keyValueStore = new KeyValueStore();
        try {
            this.listenerSocket = new ServerSocket(this.port);
            while (true) {
                Socket client = this.listenerSocket.accept();
                ServerWorker serverWorker = new ServerWorker(client);
                serverWorker.start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
                        if (request.getRequestType().equals(RequestType.PULL) || request.getRequestType().equals(RequestType.PUSH))
                        //forward it
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
