package Server.Shared.Checkpoints;

import java.util.Map;

/**
 * Created by Berkin GÜLER on 13.08.2016.
 */
public class CompressedPeriodicIncrementalCheckpoint extends PeriodicIncrementalCheckpoint {

    public CompressedPeriodicIncrementalCheckpoint(Map<String, String> currentSystemState, Map<String, String> previousSystemState) {
        super(currentSystemState, previousSystemState);
        this.checkpointData = CheckpointUtils.byteArrayToGZIPByteArray(this.checkpointData);
    }

}
