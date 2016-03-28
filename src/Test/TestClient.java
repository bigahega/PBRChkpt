package Test;

import Server.Shared.Request;
import Server.Shared.RequestType;
import Server.Shared.Response;

import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 28.03.2016.
 */
public class TestClient {

    public static void main(String[] args) throws Exception {
        Socket clientSocket = new Socket("192.95.54.27", 1881);
        ObjectOutput objectOutput = new ObjectOutputStream(clientSocket.getOutputStream());
        //Request request = new Request(RequestType.PUSH, "testkey", "testval");
        Request request = new Request(RequestType.PULL, "testkey", null);
        objectOutput.writeObject(request);
        System.out.println("object written");
        ObjectInput objectInput = new ObjectInputStream(clientSocket.getInputStream());
        Response response;
        response = (Response) objectInput.readObject();
        System.out.println(response.getResponseValue());
        objectOutput.close();
        objectInput.close();
        clientSocket.close();
    }

}
