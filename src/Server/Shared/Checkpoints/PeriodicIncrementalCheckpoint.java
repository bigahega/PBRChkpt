package Server.Shared.Checkpoints;

import java.util.Map;

/**
 * Created by bguler on 5/16/16.
 */
public class PeriodicIncrementalCheckpoint extends IncrementalCheckpoint {

    public PeriodicIncrementalCheckpoint(Map<String, String> currentSystemState, Map<String, String> previousSystemState) {
        super(currentSystemState, previousSystemState);
    }

}
