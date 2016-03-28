package Server.Shared;

import java.io.Serializable;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 28.03.2016.
 */
public class Request implements Serializable {

    private final RequestType requestType;
    private final String key;
    private final String value;

    public Request(RequestType requestType, String key, String value) {
        this.requestType = requestType;
        this.key = key;
        this.value = value;
    }

    public RequestType getRequestType() {
        return this.requestType;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

}
