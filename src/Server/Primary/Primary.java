package Server.Primary;

import Server.Shared.KeyValueStore;
import Server.Shared.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

}

class ServerWorker extends Thread {

    private Socket client;

    public ServerWorker(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(this.client.getInputStream()))) {
            while(true) {
                String line = reader.readLine();
                if(line.trim().equals("BYE"))
                    break;
            }
            this.client.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
