package Server.Shared.Checkpoints;

import java.util.Map;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 12.08.2016.
 */
public class CompressedPeriodicCheckpoint extends PeriodicCheckpoint {

    public CompressedPeriodicCheckpoint(Map<String, String> checkpointData) {
        this.checkpointData = CheckpointUtils.mapToLZMA2ByteArray(checkpointData);
    }

}
