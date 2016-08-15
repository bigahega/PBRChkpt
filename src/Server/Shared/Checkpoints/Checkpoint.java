package Server.Shared.Checkpoints;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 03.04.2016.
 */
public abstract class Checkpoint implements Serializable {

    byte[] checkpointData;

    public byte[] getCheckpointData() {
        return this.checkpointData;
    }

}
