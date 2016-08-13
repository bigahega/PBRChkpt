package Server.Shared.Checkpoints;

import java.io.*;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 12.08.2016.
 */
public class CompressedPeriodicCheckpoint extends PeriodicCheckpoint {

    public CompressedPeriodicCheckpoint(Map<String, String> checkpointData) {
        this.checkpointData = CompressionUtils.mapToCompressedByteArray(checkpointData);
    }

}
