package Server.Primary;

import Server.Shared.Checkpoints.*;
import Server.Shared.ExchangeObjects.Request;
import Server.Shared.ExchangeObjects.RequestType;
import Server.Shared.ExchangeObjects.Response;
import Server.Shared.ExchangeObjects.ResponseType;
import Server.Shared.KeyValueStore;
import org.apache.commons.io.output.CountingOutputStream;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

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
    private AtomicInteger modificationCounter = new AtomicInteger(0);
    private FileWriter fileWriter;
    private BufferedWriter bufferedWriter;
    private PrintWriter printWriter;
    private ReentrantLock checkpointLock = new ReentrantLock();
    private int testDataSize = 0;

    public Primary(List<String> serverList, Type checkpointType, int testDataSize) {
        System.out.println("Primary Server is initializing.");
        this.serverList = serverList;
        this.checkpointType = checkpointType;
        this.testDataSize = testDataSize;
        System.out.println("Test Data Size will be: " + testDataSize);
        System.out.println("Key Value Store is null. Initiating...");
        keyValueStore = new KeyValueStore();

        try {
            this.fileWriter = new FileWriter("primary_log.txt");
            this.bufferedWriter = new BufferedWriter(fileWriter);
            this.printWriter = new PrintWriter(bufferedWriter);
        } catch (IOException ex) {
            ex.printStackTrace();
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
            String value;
            value = keyValueStore.get(requestedKey);
            response = new Response(value);
        } else if (requestType.equals(RequestType.PUSH)) {
            System.out.println("It is a PUSH request.");
            String value = request.getValue();
            keyValueStore.put(requestedKey, value);
            response = new Response("OK");
        }
        return response;
    }

    private void checkpoint() {
        System.out.println("Checkpointing is initializing...");
        System.out.println("Checkpoint Type is: " + this.checkpointType.getTypeName());
        Map<String, String> currentSystemState = this.keyValueStore.getKeysValues();
        Checkpoint checkpoint;
        if (this.checkpointType.equals(FullCheckpoint.class))
            checkpoint = new FullCheckpoint(currentSystemState);
        else if (this.checkpointType.equals(PeriodicCheckpoint.class))
            checkpoint = new PeriodicCheckpoint(currentSystemState);
        else if (this.checkpointType.equals(IncrementalCheckpoint.class))
            checkpoint = new IncrementalCheckpoint(currentSystemState, this.previousSystemState);
        else if (this.checkpointType.equals(PeriodicIncrementalCheckpoint.class))
            checkpoint = new PeriodicIncrementalCheckpoint(currentSystemState, this.previousSystemState);
        else if (this.checkpointType.equals(CompressedPeriodicIncrementalCheckpoint.class))
            checkpoint = new CompressedPeriodicIncrementalCheckpoint(currentSystemState, this.previousSystemState);
        else if (this.checkpointType.equals(CompressedPeriodicCheckpoint.class))
            checkpoint = new CompressedPeriodicCheckpoint(currentSystemState);
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
        ArrayList<Long> sizes = new ArrayList<>();
        this.serverList.parallelStream().forEach(backupServer -> {
            try {
                System.out.println("Connecting to backup server: " + backupServer);
                Socket connectionToBackupServer = new Socket(backupServer, this.backupPort);
                CountingOutputStream counterStream = new CountingOutputStream(connectionToBackupServer.getOutputStream());
                ObjectOutput outputToBackupServer = new ObjectOutputStream(counterStream);
                ObjectInput inputFromBackupServer = new ObjectInputStream(connectionToBackupServer.getInputStream());
                System.out.println("Sending the checkpoint object to backup server: " + backupServer);
                outputToBackupServer.writeObject(checkpoint);
                Response response = (Response) inputFromBackupServer.readObject();
                if (!response.getResponseType().equals(ResponseType.ACK))
                    throw new IllegalStateException("Incorrect checkpoint response from server: " + backupServer);
                System.out.println("Backup server " + backupServer + " accepted the checkpoint.");
                sizes.add(counterStream.getByteCount());
                outputToBackupServer.close();
                counterStream.close();
                inputFromBackupServer.close();
                connectionToBackupServer.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        long end = System.nanoTime();
        checkpoint_count++;
        checkpoint_delay += (end - start) / 1000000;
        this.printWriter.println(String.format("%d\t%d\t%d\t%d", checkpoint_count, (end - start) / 1000000, checkpoint_delay / checkpoint_count, sizes.get(0)));
        this.printWriter.flush();
        sizes.clear();
        System.out.println("Checkpointing process delayed " + (end - start) / 1000000 + "ms");
        System.out.println("Average checkpoint delay: " + checkpoint_delay / checkpoint_count + "ms");
        this.previousSystemState = new HashMap<>(currentSystemState);
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
            try {
                ObjectInput input = new ObjectInputStream(this.client.getInputStream());
                ObjectOutput output = new ObjectOutputStream(this.client.getOutputStream());
                while (true) {
                    Object incoming = input.readObject();
                    while (checkpointLock.isLocked()) ;
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
                            printWriter.close();
                            bufferedWriter.close();
                            fileWriter.close();
                            System.exit(0);
                            break;
                        }
                    } else if (incoming instanceof Request) {
                        System.out.println("New request incoming.");
                        Request request = (Request) incoming;
                        Response response = executeWorkRequest(request);

                        if (request.getRequestType().equals(RequestType.PUSH)) {
                            if (!checkpointType.equals(PeriodicCheckpoint.class) && !checkpointType.equals(PeriodicDifferentialCheckpoint.class)
                                    && !checkpointType.equals(PeriodicIncrementalCheckpoint.class) && !checkpointType.equals(CompressedPeriodicCheckpoint.class)
                                    && !checkpointType.equals(CompressedPeriodicIncrementalCheckpoint.class))
                                checkpoint();
                            else {
                                if (modificationCounter.incrementAndGet() > testDataSize && modificationCounter.get() % CHECKPOINT_PERIOD == 0) {
                                    checkpointLock.lock();
                                    checkpoint();
                                    checkpointLock.unlock();
                                }
                            }
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