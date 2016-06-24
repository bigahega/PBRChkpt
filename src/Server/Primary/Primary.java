package Server.Primary;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 08.03.2016.
 */
public class Primary {

    private static int CHECKPOINT_PERIOD = 500;
    private static long checkpoint_count = 0;
    private static long checkpoint_delay = 0;
    private final int primaryPort = 1881;
    private final int backupPort = 1882;
    private KeyValueStore keyValueStore = null;
    private ServerSocket listenerSocket;
    private List<String> serverList;
    private Map<String, String> initalSystemState;
    private Map<String, String> previousSystemState;
    private Type checkpointType;
    private ReadWriteLock keyValueStoreReadWriteLock = new ReentrantReadWriteLock(true);
    private ReadWriteLock actionCountReadWriteLock = new ReentrantReadWriteLock(true);
    private int actionCount = 0;

    public Primary(List<String> serverList, Type checkpointType, String db_path, int db_size) {
        this(serverList, checkpointType, db_path, db_size, null);
    }

    public Primary(List<String> serverList, Type checkpointType, String db_path, int db_size, KeyValueStore newKeyValueStore) {
        System.out.println("Primary Server is initializing.");
        this.serverList = serverList;
        this.checkpointType = checkpointType;
        if (newKeyValueStore == null && this.keyValueStore == null) {
            System.out.println("Key Value Store is null. Initiating...");
            keyValueStore = new KeyValueStore();
        } else {
            System.out.println("Key Value Store is ready.");
            this.keyValueStore = newKeyValueStore;
        }
        if (db_path != null && db_size != -1) {
            try (BufferedReader reader = new BufferedReader(new FileReader(db_path))) {
                String line;
                int i = 0;
                while ((line = reader.readLine()) != null && keyValueStore.getKeysValues().size() <= db_size) {
                    try {
                        String[] matches = line.split("\t");
                        if (!matches[0].equalsIgnoreCase("\\") && Integer.parseInt(matches[0]) > 0 && matches[1].length() > 1)
                            keyValueStore.put(matches[0], matches[1]);
                    } catch (Exception ex2) {
                        ex2.printStackTrace();
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        this.initalSystemState = new HashMap<>(keyValueStore.getKeysValues());
        this.previousSystemState = new HashMap<>();
        try {
            System.out.println("Initial DB size: " + keyValueStore.getKeysValues().size());
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

    private static int sizeof(Object obj) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            objectOutputStream.close();

            return byteArrayOutputStream.toByteArray().length;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
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
        Map<String, String> currentSystemState = new HashMap<>(keyValueStore.getKeysValues());
        Checkpoint checkpoint;
        if (this.checkpointType.equals(FullCheckpoint.class))
            checkpoint = new FullCheckpoint(currentSystemState);
        else if (this.checkpointType.equals(PeriodicCheckpoint.class))
            checkpoint = new PeriodicCheckpoint(currentSystemState);
        else if (this.checkpointType.equals(IncrementalCheckpoint.class))
            checkpoint = new IncrementalCheckpoint(currentSystemState, this.previousSystemState);
        else if (this.checkpointType.equals(PeriodicIncrementalCheckpoint.class))
            checkpoint = new PeriodicIncrementalCheckpoint(currentSystemState, this.previousSystemState);
        else if (this.checkpointType.equals(DifferentialCheckpoint.class))
            if (checkpoint_count == 0)
                checkpoint = new DifferentialCheckpoint(new HashMap<>(), currentSystemState);
            else
                checkpoint = new DifferentialCheckpoint(this.initalSystemState, currentSystemState);
        else if (this.checkpointType.equals(PeriodicDifferentialCheckpoint.class))
            if (checkpoint_count == 0)
                checkpoint = new DifferentialCheckpoint(new HashMap<>(), currentSystemState);
            else
                checkpoint = new DifferentialCheckpoint(this.initalSystemState, currentSystemState);
        else
            return;

        long start = System.nanoTime();
        this.serverList.parallelStream().forEach(backupServer -> {
            try {
                System.out.println("Connecting to backup server: " + backupServer);
                Socket connectionToBackupServer = new Socket(backupServer, this.backupPort);
                ObjectOutput outputToBackupServer = new ObjectOutputStream(connectionToBackupServer.getOutputStream());
                System.out.println("Sending the checkpoint object to backup server: " + backupServer);
                outputToBackupServer.writeObject(checkpoint);
                ObjectInput inputFromBackupServer = new ObjectInputStream(connectionToBackupServer.getInputStream());
                Response response = (Response) inputFromBackupServer.readObject();
                if (!response.getResponseType().equals(ResponseType.ACK))
                    throw new IllegalStateException("Incorrect checkpoint response from server: " + backupServer);
                System.out.println("Backup server " + backupServer + " accepted the checkpoint.");
                outputToBackupServer.close();
                inputFromBackupServer.close();
                connectionToBackupServer.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        long end = System.nanoTime();
        checkpoint_count++;
        checkpoint_delay += (end - start) / 1000000;
        System.out.println("Checkpoint size is: " + sizeof(checkpoint) + "bytes");
        System.out.println("Checkpointing process delayed " + (end - start) / 1000000 + "ms");
        System.out.println("Average checkpoint delay: " + checkpoint_delay / checkpoint_count + "ms");
        this.previousSystemState = new HashMap<>(currentSystemState);
        System.out.println("Releasing the write-lock...");
        keyValueStoreReadWriteLock.writeLock().unlock();
    }

    public void setKeyValueStore(KeyValueStore newKeyValueStore) {
        keyValueStore = newKeyValueStore;
    }

    private class ServerWorker extends Thread {

        private Socket client;

        public ServerWorker(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try (ObjectInput input = new ObjectInputStream(this.client.getInputStream())) {
                ObjectOutput output = new ObjectOutputStream(this.client.getOutputStream());
                while (true) {
                    Object incoming = input.readObject();
                    if (incoming instanceof Integer) {
                        if ((int) incoming == 0xDEADBABA) {
                            System.out.println("It is a kill request. Bye!");
                            serverList.parallelStream().forEach(backupServer -> {
                                try {
                                    System.out.println("Connecting to backup server: " + backupServer);
                                    Socket connectionToBackupServer = new Socket(backupServer, backupPort);
                                    ObjectOutput outputToBackupServer = new ObjectOutputStream(connectionToBackupServer.getOutputStream());
                                    outputToBackupServer.writeObject(0xDEADBABA);
                                    connectionToBackupServer.close();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            });
                            System.exit(0);
                            break;
                        }
                    } else if (incoming instanceof Request) {
                        System.out.println("New request incoming.");
                        Request request = (Request) incoming;
                        Response response = executeWorkRequest(request);

                        actionCountReadWriteLock.writeLock().lock();
                        actionCount++;
                        actionCountReadWriteLock.writeLock().unlock();
                        if (!checkpointType.equals(PeriodicCheckpoint.class) && !checkpointType.equals(PeriodicDifferentialCheckpoint.class)
                                && !checkpointType.equals(PeriodicIncrementalCheckpoint.class))
                            checkpoint();
                        else {
                            actionCountReadWriteLock.writeLock().lock();
                            if (actionCount % CHECKPOINT_PERIOD == 0) {
                                checkpoint();
                                actionCount = 0;
                            }
                            actionCountReadWriteLock.writeLock().unlock();
                        }
                        output.writeObject(response);
                    } else
                        break;
                    System.out.println("DB Size: " + keyValueStore.getKeysValues().size());
                }
                output.close();
                this.client.close();
            } catch (Exception ex) {
                if (ex instanceof SocketException && ex.getMessage().contains("closed"))
                    System.out.println("Client is gone.");
                else if (!(ex instanceof EOFException))
                    ex.printStackTrace();
            }
        }
    }
}