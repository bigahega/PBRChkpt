package Server.Shared.Checkpoints;

import java.io.Serializable;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 03.04.2016.
 */
public abstract class Checkpoint implements Serializable {

    static boolean periodic = false;
    byte[] checkpointData;

    public byte[] getCheckpointData() {
        return this.checkpointData;
    }

    public boolean isPeriodic() {
        return periodic;
    }

}
