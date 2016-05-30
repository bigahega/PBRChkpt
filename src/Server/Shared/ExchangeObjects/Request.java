package Server.Shared.ExchangeObjects;

import org.javatuples.Pair;

import java.io.Serializable;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 28.03.2016.
 */
public class Request implements Serializable {

    private final RequestType requestType;
    private final Object data;
    private String key;
    private String value;

    public Request(RequestType requestType, Object data) {
        this.requestType = requestType;
        this.data = data;
        if (!requestType.equals(RequestType.CHECKPOINT)) {
            Pair<String, String> keyValue = (Pair<String, String>) data;
            this.key = keyValue.getValue0();
            this.value = keyValue.getValue1();
        }
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

    public Object getData() {
        return this.data;
    }

}
