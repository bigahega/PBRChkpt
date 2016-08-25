package Server.Backup;

import Server.Primary.Primary;
import Server.Shared.Checkpoints.*;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Primary p = new Primary(this.serverList, this.checkpointType, null, -1, this.keyValueStore);
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

        Type checkpointType = this.checkpointList.get(0).getClass();
        long startRestore = System.nanoTime();
        System.out.println("Found checkpoint type: " + checkpointType.getTypeName());
        Map<String, Map<String, String>> builtSystemState = new HashMap<>();
        if (checkpointType.equals(FullCheckpoint.class) || checkpointType.equals(PeriodicCheckpoint.class)) {
            keyValueStore.restoreCheckpoint(CheckpointUtils.byteArrayToMap(this.checkpointList.get(this.checkpointList.size() - 1).getCheckpointData()));
            System.out.println("Built system state size: " + this.keyValueStore.getKeysValues().size());
            this.checkpointList.clear();
        } else if (checkpointType.equals(IncrementalCheckpoint.class) || checkpointType.equals(PeriodicIncrementalCheckpoint.class)) {
            for (Checkpoint checkpoint : this.checkpointList)
                builtSystemState.putAll(CheckpointUtils.byteArrayToMap(checkpoint.getCheckpointData()));
            System.out.println("Built system state size: " + builtSystemState.size());
            keyValueStore.restoreCheckpoint(builtSystemState);
        } else if(checkpointType.equals(CompressedPeriodicIncrementalCheckpoint.class)) {
            for(Checkpoint checkpoint : this.checkpointList)
                builtSystemState.putAll(CheckpointUtils.compressedByteArrayToMap(checkpoint.getCheckpointData()));
            System.out.println("Built system state size: " + builtSystemState.size());
        } else if (checkpointType.equals(DifferentialCheckpoint.class) || checkpointType.equals(DifferentialCheckpoint.class)) {
            Checkpoint first = this.checkpointList.get(0);
            Checkpoint last = this.checkpointList.get(1);
            builtSystemState.putAll(CheckpointUtils.byteArrayToMap(first.getCheckpointData()));
            builtSystemState.putAll(CheckpointUtils.byteArrayToMap(last.getCheckpointData()));
            System.out.println("Built system state size: " + builtSystemState.size());
            keyValueStore.restoreCheckpoint(builtSystemState);
        }
        long endRestore = System.nanoTime();
        System.out.println("Restored the Key-Value DB using checkpoints successfully in " +
                (float) (endRestore - startRestore) / 1000000.f + "ms");
    }

    private class ServerWorker extends Thread {

        private Socket client;

        private ServerWorker(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try (ObjectInput input = new ObjectInputStream(this.client.getInputStream())) {
                while (true) {
                    Object incoming = input.readObject();
                    if (incoming instanceof Integer) {
                        if ((int) incoming == 0xDEADBABA) {
                            System.exit(0);
                            break;
                        }
                    } else if (incoming instanceof Request) {
                        Request request = (Request) incoming;
                        if (request.getRequestType().equals(RequestType.SELECT) || request.getRequestType().equals(RequestType.UPDATE)) {
                            System.out.println("Primary is dead! I am the new Primary!");
                            Response response = takeover(request);
                            ObjectOutput responseToClient = new ObjectOutputStream(this.client.getOutputStream());
                            responseToClient.writeObject(response);
                            responseToClient.close();
                            close();
                            break;
                        }
                    } else if (incoming instanceof Checkpoint) {
                        Checkpoint checkpoint = (Checkpoint) incoming;
                        System.out.println("We have a new checkpoint request...");
                        if (checkpointType.equals(FullCheckpoint.class) || checkpointType.equals(PeriodicCheckpoint.class))
                            checkpointList.clear();
                        else if (checkpointType.equals(DifferentialCheckpoint.class)) {
                            if (checkpointList.size() > 0) {
                                Checkpoint initial = checkpointList.get(0);
                                checkpointList.clear();
                                checkpointList.add(initial);
                            }
                        }
                        System.out.print("Adding it to the checkpoint list...");
                        checkpointList.add(checkpoint);
                        System.out.println("OK");

                        if (checkpointType.equals(CompressedPeriodicIncrementalCheckpoint.class))
                            System.out.println("Checkpoint data count: " + CheckpointUtils.compressedByteArrayToMap(checkpoint.getCheckpointData()).keySet().size());
                        else
                            System.out.println("Checkpoint data count: " + CheckpointUtils.byteArrayToMap(checkpoint.getCheckpointData()).keySet().size());
                        ObjectOutput responseToPrimary = new ObjectOutputStream(this.client.getOutputStream());
                        Response response = new Response(ResponseType.ACK);
                        System.out.println("ACK is sent!");
                        responseToPrimary.writeObject(response);
                        responseToPrimary.close();
                    }
                }
            } catch (EOFException | SocketException ex) {
                //asdfafsd
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
