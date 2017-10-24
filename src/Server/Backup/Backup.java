package Server.Backup;

import Server.Primary.Primary;
import Server.Shared.Checkpoints.Checkpoint;
import Server.Shared.Checkpoints.FullCheckpoint;
import Server.Shared.Checkpoints.PeriodicCheckpoint;
import Server.Shared.ExchangeObjects.Request;
import Server.Shared.ExchangeObjects.RequestType;
import Server.Shared.ExchangeObjects.Response;
import Server.Shared.ExchangeObjects.ResponseType;
import Server.Shared.KeyValueStore;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 08.03.2016.
 */
public class Backup {

    private final int primaryPort = 1881;
    private final int backupPort = 1882;
    private final String baseFilePath = "/home/ku_distributed";
    private KeyValueStore keyValueStore = null;
    private ServerSocket listenerSocket;
    private List<String> serverList;
    private List<Checkpoint> checkpointList;
    private Type checkpointType;
    private boolean close = false;

    public Backup(List<String> serverList, Type checkpointType) {
        System.out.println("Backup Server is initializing...");
        this.serverList = serverList;
        if (keyValueStore == null)
            keyValueStore = new KeyValueStore();
        this.checkpointList = new ArrayList<>();
        this.checkpointType = checkpointType;
        try {
            this.listenerSocket = new ServerSocket(this.backupPort);
            System.out.println("Backup socket created...");
            while (true) {
                if (close)
                    break;
                Socket client = this.listenerSocket.accept();
                ServerWorker serverWorker = new ServerWorker(client);
                serverWorker.start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void close() {
        System.out.print("Closing backup server...");
        this.close = true;
        try {
            this.listenerSocket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("Done.");
    }

    private Response takeover(Request request) {
        this.restoreCheckpoints();
        Primary p = new Primary(this.serverList, this.checkpointType, 0);
        p.setKeyValueStore(keyValueStore);
        System.out.println("work work");
        return p.executeWorkRequest(request);
    }

    private void restoreCheckpoints() {
        System.out.println("Restoring checkpoints...");
        if (this.checkpointList.size() == 0) {
            System.out.println("No checkpoint found. Nothing to restore...");
            return;
        }
        //TODO: fix this shit
    }

    private class ServerWorker extends Thread {

        private Socket client;

        private ServerWorker(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                ObjectInput input = new ObjectInputStream(this.client.getInputStream());
                ObjectOutput output = new ObjectOutputStream(this.client.getOutputStream());
                while (true) {
                    Object incoming = input.readObject();
                    if (incoming instanceof Integer) {
                        if ((int) incoming == 0xDEADBABA) {
                            System.exit(0);
                            break;
                        }
                    } else if (incoming instanceof Request) {
                        Request request = (Request) incoming;
                        if (request.getRequestType().equals(RequestType.PULL) || request.getRequestType().equals(RequestType.PUSH)) {
                            System.out.println("Primary is dead! I am the new Primary!");
                            Response response = takeover(request);
                            output.writeObject(response);
                            output.close();
                            close();
                            break;
                        }
                    } else if (incoming instanceof Checkpoint) {
                        Checkpoint checkpoint = (Checkpoint) incoming;
                        System.out.println("We have a new checkpoint request...");
                        if (checkpointType.equals(FullCheckpoint.class) || checkpointType.equals(PeriodicCheckpoint.class))
                            checkpointList.clear();
                        System.out.print("Adding it to the checkpoint list...");
                        checkpointList.add(checkpoint);
                        System.out.println("OK");
                        Response response = new Response(ResponseType.ACK);
                        System.out.println("ACK is sent!");
                        output.writeObject(response);
                        output.close();
                    }
                }
            } catch (SocketException | EOFException ex) {
                //asdfafsd
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
