package Test;

import Server.Shared.ExchangeObjects.Request;
import Server.Shared.ExchangeObjects.RequestType;
import Server.Shared.ExchangeObjects.Response;
import org.javatuples.Pair;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

/**
 * Created by bguler on 4/25/16.
 */
public class TestClient {

    private static HashMap<String, String> db = new HashMap<>();
    private static Float average_delay = 0.0f;
    private static Integer req_count = 0;

    public static void main(String[] args) throws Exception {
        String filename = args[0];
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String[] matches = line.split("\t");
                    if (!matches[0].equalsIgnoreCase("\\") && Integer.parseInt(matches[0]) > 0 && matches[1].length() > 1)
                        db.put(matches[0], matches[1]);
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("Reading finished");
        Socket clientSocket = new Socket("planetlab-01.cs.princeton.edu", 1881);
        ObjectOutput objectOutput = new ObjectOutputStream(clientSocket.getOutputStream());
        ObjectInput objectInput = new ObjectInputStream(clientSocket.getInputStream());
        int i = 0;
        dblooper:
        for (String key : db.keySet()) {
            boolean next = false;
            while (!next) {
                try {
                    if (i <= 60000) {
                        i++;
                        continue;
                    }
                    if (req_count >= 10000) {
                        objectOutput.writeObject(0xDEADBABA);
                        break dblooper;
                    }

                    if (!clientSocket.isConnected()) {
                        System.out.println("Connnecting to primary...");
                        clientSocket = new Socket("planetlab-01.cs.princeton.edu", 1881);
                        objectOutput = new ObjectOutputStream(clientSocket.getOutputStream());
                        System.out.println("streams created");
                    }

                    long start = System.nanoTime();
                    System.out.println("creating request");
                    Request request = new Request(RequestType.PUSH, new Pair<>(key, db.get(key)));
                    objectOutput.writeObject(request);
                    System.out.println("object written");
                    if (!clientSocket.isConnected())
                        objectInput = new ObjectInputStream(clientSocket.getInputStream());
                    Response response;
                    response = (Response) objectInput.readObject();
                    System.out.println(response.getResponseValue());
                    long end = System.nanoTime();
                    req_count++;
                    average_delay += (end - start) / 1000000;
                    System.out.println("Request delay: " + (float) (end - start) / 1000000.f + "ms");
                    System.out.println("Average delay: " + average_delay / req_count + "ms");
                    System.out.println("Req count: " + req_count);
                    next = true;
                } catch (EOFException ex) {
                    System.out.println("Streams sucked up...");
                    Thread.sleep(100);
                    continue;
                } catch (StreamCorruptedException ex) {
                    System.out.println("Streams sucked up...");
                    clientSocket = new Socket("planet1.pnl.nitech.ac.jp", 1881);
                    objectOutput = new ObjectOutputStream(clientSocket.getOutputStream());
                    objectInput = new ObjectInputStream(clientSocket.getInputStream());
                    Thread.sleep(100);
                } catch (SocketException ex) {
                    System.out.println("Socket sucked up...Trying to reboot it...");
                    clientSocket = new Socket("planet1.pnl.nitech.ac.jp", 1881);
                    objectOutput = new ObjectOutputStream(clientSocket.getOutputStream());
                    objectInput = new ObjectInputStream(clientSocket.getInputStream());
                    Thread.sleep(100);
                }
            }
        }
        System.out.println("DONE");
        objectInput.close();
        objectOutput.close();
        clientSocket.close();
    }

}
