package Server.Shared.Checkpoints;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 03.04.2016.
 */
public class IncrementalCheckpoint extends Checkpoint {

    public IncrementalCheckpoint(Map<String, String> currentSystemState, Map<String, String> previousSystemState) {
        ConcurrentHashMap<String, String> difference = new ConcurrentHashMap<>();
        currentSystemState.keySet().parallelStream().forEach(key -> {
            if (!previousSystemState.containsKey(key))
                difference.put(key, currentSystemState.get(key));
            else if (!previousSystemState.get(key).equals(currentSystemState.get(key)))
                difference.put(key, currentSystemState.get(key));
        });

        this.checkpointData = CheckpointUtils.mapToByteArray(difference);
    }

}
