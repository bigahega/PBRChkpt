package Server.Shared;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 10.03.2016.
 */
public class KeyValueStore implements Serializable {

    private Map<String, String> keysValues;

    public KeyValueStore()
    {
        this.keysValues = new HashMap<>();
    }

    public void put(String key, String Value)
    {
        this.keysValues.put(key, Value);
    }

    public String get(String key)
    {
        return this.keysValues.get(key);
    }

    public Map<String, String> getKeysValues() {
        return this.keysValues;
    }

    public void restoreCheckpoint(Map<String, String> checkpoint) {
        this.keysValues = checkpoint;
    }

}
