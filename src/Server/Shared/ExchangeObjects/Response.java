package Server.Shared.ExchangeObjects;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 28.03.2016.
 */
public class Response implements Serializable {

    private Map<String, String> responseValue;

    private ResponseType responseType;

    public Response(Map<String, String> responseValue) {
        this.responseValue = responseValue;
    }

    public Response(ResponseType responseType) {
        this.responseType = responseType;
    }

    public Map<String, String> getResponseValue() {
        return this.responseValue;
    }

    public ResponseType getResponseType() {
        return this.responseType;
    }

}
