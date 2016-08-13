package Server.Shared.Checkpoints;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 03.04.2016.
 */
public abstract class Checkpoint implements Serializable {

    byte[] checkpointData;

    public byte[] getCheckpointData() {
        return this.checkpointData;
    }

    byte[] mapToByteArray(Map<String, String> map) {
        byte[] result = null;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(map);
            result = baos.toByteArray();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return result;
    }

}
