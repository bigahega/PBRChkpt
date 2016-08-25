package Server.Shared.Checkpoints;

import java.util.Map;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 03.04.2016.
 */
public class FullCheckpoint extends Checkpoint {

    public FullCheckpoint(Map<String, Map<String, String>> checkpointData) {
        this.checkpointData = CheckpointUtils.mapToByteArray(checkpointData);
    }
}
