package Server.Shared.Checkpoints;

import java.util.Map;

/**
 * Created by bguler on 5/16/16.
 */
public class PeriodicDifferentialCheckpoint extends DifferentialCheckpoint {

    public PeriodicDifferentialCheckpoint(Map<String, String> initialSystemState, Map<String, String> currentSystemState) {
        super(initialSystemState, currentSystemState);
    }

}
