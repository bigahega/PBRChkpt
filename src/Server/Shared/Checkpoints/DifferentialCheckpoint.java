package Server.Shared.Checkpoints;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 03.04.2016.
 */
public class DifferentialCheckpoint extends Checkpoint {

    public DifferentialCheckpoint(Map<String, String> initialSystemState, Map<String, String> currentSystemState) {
        Map<String, String> difference = new HashMap<>();
        for (String key : currentSystemState.keySet())
            if (!initialSystemState.containsKey(key))
                difference.put(key, currentSystemState.get(key));
            else if (!initialSystemState.get(key).equals(currentSystemState.get(key)))
                difference.put(key, currentSystemState.get(key));

        CheckpointUtils.mapToByteArray(difference);
    }

}
