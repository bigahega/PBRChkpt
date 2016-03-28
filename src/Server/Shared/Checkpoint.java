package Server.Shared;

import java.io.Serializable;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 28.03.2016.
 */
public class Checkpoint implements Serializable {

    private CheckpointType checkpointType;
    private Object data;

    public Checkpoint(CheckpointType checkpointType, Object data) {
        this.checkpointType = checkpointType;
    }

    public Object getData() {
        return this.data;
    }

}
