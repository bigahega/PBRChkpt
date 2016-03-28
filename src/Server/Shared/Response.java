package Server.Shared;

import java.io.Serializable;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 28.03.2016.
 */
public class Response implements Serializable {

    private final String responseValue;

    public Response(String responseValue) {
        this.responseValue = responseValue;
    }

    public String getResponseValue() {
        return this.responseValue;
    }

}
