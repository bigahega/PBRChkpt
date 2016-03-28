package Server.Shared;

import java.io.Serializable;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 28.03.2016.
 */
public class Request implements Serializable {

    private final String key;

    public Request(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

}
