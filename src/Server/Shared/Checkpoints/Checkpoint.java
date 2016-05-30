package Server.Shared.Checkpoints;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 03.04.2016.
 */
public abstract class Checkpoint implements Serializable {

    protected Map<String, String> checkpointData;

    public Map<String, String> getCheckpointData() {
        return this.checkpointData;
    }

}
