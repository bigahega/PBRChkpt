package Server.Primary;

import Server.Shared.*;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 08.03.2016.
 */
public class Primary extends Server {

    private static int CHECKPOINT_PERIOD = 5;
    private List<String> serverList;
    private Map<String, String> initalSystemState;
    private Map<String, String> previousSystemState;
    private Type checkpointType;
    private ReadWriteLock keyValueStoreReadWriteLock = new ReentrantReadWriteLock(true);
    private ReadWriteLock actionCountReadWriteLock = new ReentrantReadWriteLock(true);
    private int actionCount = 0;

    public Primary(List<String> serverList, Type checkpointType) {
        System.out.println("Primary Server is initializing.");
        this.serverList = serverList;
        this.checkpointType = checkpointType;
        if (this.keyValueStore == null)
            this.keyValueStore = new KeyValueStore();
        this.initalSystemState = new HashMap<>(this.keyValueStore.getKeysValues());
        this.previousSystemState = this.initalSystemState;
        try {
            this.listenerSocket = new ServerSocket(this.primaryPort);
            System.out.println("Socket " + this.primaryPort + " is created.");
            while (true) {
                Socket client = this.listenerSocket.accept();
                ServerWorker serverWorker = new ServerWorker(client);
                serverWorker.start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Response executeWorkRequest(Request request) {
        Response response = null;
        RequestType requestType = request.getRequestType();
        String requestedKey = request.getKey();
        if (requestType.equals(RequestType.PULL)) {
            System.out.println("It is a PULL request.");
            System.out.print("Trying to get a read-lock...");
            keyValueStoreReadWriteLock.readLock().lock();
            System.out.println("Done.");
            String value;
            value = keyValueStore.get(requestedKey);
            response = new Response(value);
            System.out.print("Releasing the read-lock...");
            keyValueStoreReadWriteLock.readLock().unlock();
            System.out.println("Done.");
        } else if (requestType.equals(RequestType.PUSH)) {
            System.out.println("It is a PUSH request.");
            System.out.print("Trying to get a write-lock...");
            keyValueStoreReadWriteLock.writeLock().lock();
            System.out.println("Done.");
            String value = request.getValue();
            keyValueStore.put(requestedKey, value);
            response = new Response("OK");
            System.out.print("Releasing the write-lock...");
            keyValueStoreReadWriteLock.writeLock().unlock();
            System.out.println("Done.");
        }
        return response;
    }

    private void checkpoint() {
        System.out.println("Checkpointing is initializing...");
        System.out.println("Checkpoint Type is: " + this.checkpointType.getTypeName());
        System.out.print("Trying to get a write-lock...");
        keyValueStoreReadWriteLock.writeLock().lock();
        System.out.println("Done.");
        Map<String, String> currentSystemState = this.keyValueStore.getKeysValues();
        Checkpoint checkpoint;
        if (this.checkpointType.equals(FullCheckpoint.class))
            checkpoint = new FullCheckpoint(currentSystemState);
        else if (this.checkpointType.equals(PeriodicCheckpoint.class))
            checkpoint = new PeriodicCheckpoint(currentSystemState);
        else if (this.checkpointType.equals(IncrementalCheckpoint.class))
            checkpoint = new IncrementalCheckpoint(currentSystemState, this.previousSystemState);
        else if (this.checkpointType.equals(DifferentialCheckpoint.class))
            checkpoint = new DifferentialCheckpoint(this.initalSystemState, currentSystemState);
        else
            return;

        this.serverList.parallelStream().forEach(backupServer -> {
            try {
                System.out.println("Connecting to backup server: " + backupServer);
                Socket connectionToBackupServer = new Socket(backupServer, this.backupPort);
                ObjectOutput outputToBackupServer = new ObjectOutputStream(connectionToBackupServer.getOutputStream());
                System.out.println("Sending the checkpoint object to backup server: " + backupServer);
                outputToBackupServer.writeObject(checkpoint);
                ObjectInput inputFromBackupServer = new ObjectInputStream(connectionToBackupServer.getInputStream());
                Response response = (Response) inputFromBackupServer.readObject();
                if (!response.getResponseValue().equals("CHECKPOINT OK"))
                    throw new IllegalStateException("Incorrect checkpoint response from server: " + backupServer);
                System.out.println("Backup server " + backupServer + " accepted the checkpoint.");
                outputToBackupServer.close();
                inputFromBackupServer.close();
                connectionToBackupServer.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        this.previousSystemState = currentSystemState;
        System.out.println("Releasing the write-lock...");
        keyValueStoreReadWriteLock.writeLock().unlock();
    }

    private class ServerWorker extends Thread {

        private Socket client;

        public ServerWorker(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            System.out.println("New request incoming.");
            try (ObjectInput input = new ObjectInputStream(this.client.getInputStream())) {
                while (true) {
                    Object incoming = input.readObject();
                    if (incoming instanceof Integer) {
                        if ((int) incoming == 0xDEADBABA) {
                            System.out.println("It is a kill request. Bye!");
                            break;
                        }
                    } else if (incoming instanceof Request) {
                        Request request = (Request) incoming;
                        Response response = executeWorkRequest(request);
                        ObjectOutput output = new ObjectOutputStream(this.client.getOutputStream());
                        output.writeObject(response);
                        output.close();
                        actionCountReadWriteLock.writeLock().lock();
                        actionCount++;
                        actionCountReadWriteLock.writeLock().unlock();
                        if (!checkpointType.equals(PeriodicCheckpoint.class))
                            checkpoint();
                        else {
                            actionCountReadWriteLock.writeLock().lock();
                            if (actionCount % CHECKPOINT_PERIOD == 0) {
                                checkpoint();
                                actionCount = 0;
                            }
                            actionCountReadWriteLock.writeLock().unlock();
                        }
                    } else
                        break;
                }
                this.client.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}