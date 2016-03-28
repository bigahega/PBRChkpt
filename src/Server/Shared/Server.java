package Server.Shared;

import java.net.ServerSocket;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 08.03.2016.
 */
public abstract class Server {

    protected final int port = 1881;
    protected KeyValueStore keyValueStore;
    protected ServerSocket listenerSocket;

}
