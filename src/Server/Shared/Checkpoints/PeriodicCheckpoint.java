package Server.Shared.Checkpoints;

import java.util.Map;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 04.04.2016.
 */
public class PeriodicCheckpoint extends Checkpoint {

    public PeriodicCheckpoint(Map<String, String> checkpointData) {
        this.checkpointData = CheckpointUtils.mapToByteArray(checkpointData);
    }

    PeriodicCheckpoint() {
    }
}
