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
        int max_req_limit = Integer.parseInt(args[1]);
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

        Socket clientSocket = new Socket("saturn.planetlab.carleton.ca", 1881);
        ObjectOutput objectOutput = new ObjectOutputStream(clientSocket.getOutputStream());
        ObjectInput objectInput = new ObjectInputStream(clientSocket.getInputStream());

        FileWriter fileWriter = new FileWriter("client_log.txt");
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        PrintWriter printWriter = new PrintWriter(bufferedWriter);

        int i = 0;
        for (String key : db.keySet()) {
            try {
                if (req_count >= max_req_limit) {
                    objectOutput.writeObject(0xDEADBABA);
                    break;
                }

                long start = System.nanoTime();
                Request request = new Request(RequestType.PUSH, new Pair<>(key, db.get(key)));
                objectOutput.writeObject(request);

                Response response;
                response = (Response) objectInput.readObject();
                System.out.println(response.getResponseValue());
                long end = System.nanoTime();

                req_count++;
                average_delay += (end - start) / 1000000.f;
                float req_delay = (float) (end - start) / 1000000.f;
                printWriter.println(String.format("%d\t%f\t%f", req_count, req_delay, average_delay / req_count));

            } catch (EOFException | StreamCorruptedException ex) {
                System.out.println("Streams sucked up...");
            } catch (SocketException ex) {
                System.out.println("Socket sucked up...Trying to reboot it...");
            }
        }
        printWriter.close();
        bufferedWriter.close();
        fileWriter.close();

        objectInput.close();
        objectOutput.close();
        clientSocket.close();
    }

}
