package Server.Shared.Checkpoints;

import java.util.Map;

/**
 * Created by Berkin GÜLER on 13.08.2016.
 */
public class CompressedPeriodicIncrementalCheckpoint extends PeriodicIncrementalCheckpoint {

    public CompressedPeriodicIncrementalCheckpoint(Map<String, String> currentSystemState, Map<String, String> previousSystemState) {
        super(currentSystemState, previousSystemState);
        byte[] compressed = CheckpointUtils.GZIPcompressByteArray(this.checkpointData);
        System.arraycopy(compressed, 0, this.checkpointData, 0, compressed.length);
    }

}
