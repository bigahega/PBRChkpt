package Test;

import Server.Primary.Primary;
import Server.Shared.Checkpoints.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 28.03.2016.
 */

public class TestPrimary {

    private static List<String> backupList = new ArrayList<>();

    public static void main(String[] args) {
        backupList.add("planetlab3.rutgers.edu");
        backupList.add("ricepl-5.cs.rice.edu");
        backupList.add("planetlab01.cs.washington.edu");
        Primary p;
        int testDataSize = Integer.parseInt(args[1]);
        switch (args[0]) {
            case "full":
                p = new Primary(backupList, FullCheckpoint.class, testDataSize);
                break;
            case "periodic":
                p = new Primary(backupList, PeriodicCheckpoint.class, testDataSize);
                break;
            case "incremental":
                p = new Primary(backupList, IncrementalCheckpoint.class, testDataSize);
                break;
            case "differential":
                p = new Primary(backupList, DifferentialCheckpoint.class, testDataSize);
                break;
            case "pincremental":
                p = new Primary(backupList, PeriodicIncrementalCheckpoint.class, testDataSize);
                break;
            case "cpincremental":
                p = new Primary(backupList, CompressedPeriodicIncrementalCheckpoint.class, testDataSize);
                break;
            case "cpi":
                p = new Primary(backupList, CompressedPeriodicCheckpoint.class, testDataSize);
                break;
            case "pdifferential":
                p = new Primary(backupList, PeriodicDifferentialCheckpoint.class, testDataSize);
                break;
        }
    }

}
