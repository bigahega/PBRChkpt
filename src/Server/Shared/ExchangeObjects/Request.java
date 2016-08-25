package Server.Shared.ExchangeObjects;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 28.03.2016.
 */
public class Request implements Serializable {

    private final RequestType requestType;
    private String key;
    private Map<String, String> values;

    public Request(RequestType requestType, String key, Map<String, String> vals) {
        this.requestType = requestType;
        if (!requestType.equals(RequestType.CHECKPOINT)) {
            this.key = key;
            this.values = vals;
        }
    }

    public RequestType getRequestType() {
        return this.requestType;
    }

    public String getKey() {
        return this.key;
    }

    public Map<String, String> getValue() {
        return this.values;
    }

}
