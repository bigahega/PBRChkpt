package Server.Backup;

import Server.Primary.Primary;
import Server.Shared.*;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 08.03.2016.
 */
public class Backup extends Server {

    private List<String> serverList;
    private List<Checkpoint> checkpointList;
    private Type checkpointType;
    private boolean close = false;

    public Backup(List<String> serverList, Type checkpointType) {
        this.serverList = serverList;
        if (this.keyValueStore == null)
            this.keyValueStore = new KeyValueStore();
        this.checkpointList = new ArrayList<>();
        this.checkpointType = checkpointType;
        try {
            this.listenerSocket = new ServerSocket(this.backupPort);
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

    public void close() {
        this.close = true;
    }

    public Response takeover(Request request) {
        this.restoreCheckpoints();
        Primary p = new Primary(this.serverList, this.checkpointType);
        return p.executeWorkRequest(request);
    }

    public void restoreCheckpoints() {
        if (this.checkpointList.size() == 0)
            return;

        Type checkpointType = this.checkpointList.get(0).getClass();
        if (checkpointType.equals(FullCheckpoint.class) || checkpointType.equals(PeriodicCheckpoint.class)) {
            this.keyValueStore.restoreCheckpoint(this.checkpointList.get(this.checkpointList.size() - 1).getCheckpointData());
            this.checkpointList.clear();
        } else if (checkpointType.equals(IncrementalCheckpoint.class)) {
            Map<String, String> builtSystemState = new HashMap<>();
            for (Checkpoint checkpoint : this.checkpointList)
                builtSystemState.putAll(checkpoint.getCheckpointData());
            this.keyValueStore.restoreCheckpoint(builtSystemState);
        } else if (checkpointType.equals(DifferentialCheckpoint.class)) {
            Map<String, String> builtSystemState = new HashMap<>();
            Checkpoint first = this.checkpointList.get(0);
            Checkpoint last = this.checkpointList.get(this.checkpointList.size() - 1);
            builtSystemState.putAll(first.getCheckpointData());
            builtSystemState.putAll(last.getCheckpointData());
            this.keyValueStore.restoreCheckpoint(builtSystemState);
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
                        if (request.getRequestType().equals(RequestType.PULL) || request.getRequestType().equals(RequestType.PUSH)) {
                            Response response = takeover(request);
                            ObjectOutput responseToClient = new ObjectOutputStream(this.client.getOutputStream());
                            responseToClient.writeObject(response);
                            responseToClient.close();
                            close();
                            break;
                        } else {
                            checkpointList.add((Checkpoint) request.getData());
                            ObjectOutput responseToPrimary = new ObjectOutputStream(this.client.getOutputStream());
                            Response response = new Response(ResponseType.ACK);
                            responseToPrimary.writeObject(response);
                            responseToPrimary.close();
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
