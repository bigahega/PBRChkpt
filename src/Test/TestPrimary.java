package Test;

import Server.Primary.Primary;
import Server.Shared.Checkpoints.DifferentialCheckpoint;
import Server.Shared.Checkpoints.FullCheckpoint;
import Server.Shared.Checkpoints.IncrementalCheckpoint;
import Server.Shared.Checkpoints.PeriodicCheckpoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 28.03.2016.
 */

public class TestPrimary {

    private static List<String> backupList = new ArrayList<>();

    public static void main(String[] args) {
        backupList.add("planetlab3.rutgers.edu");
        backupList.add("saturn.planetlab.carleton.ca");
        backupList.add("planetlab01.cs.washington.edu");
        //Primary p = new Primary(backupList, FullCheckpoint.class, "/home/ku_distributed/db.txt", 50000);
        Primary p;
        switch (args[0]) {
            case "full":
                p = new Primary(backupList, FullCheckpoint.class, null, -1);
                break;
            case "periodic":
                p = new Primary(backupList, PeriodicCheckpoint.class, null, -1);
                break;
            case "incremental":
                p = new Primary(backupList, IncrementalCheckpoint.class, null, -1);
                break;
            case "differential":
                p = new Primary(backupList, DifferentialCheckpoint.class, null, -1);
                break;
        }
    }

}
