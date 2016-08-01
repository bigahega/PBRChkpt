package Test;

import Server.Backup.Backup;
import Server.Shared.Checkpoints.DifferentialCheckpoint;
import Server.Shared.Checkpoints.FullCheckpoint;
import Server.Shared.Checkpoints.IncrementalCheckpoint;
import Server.Shared.Checkpoints.PeriodicCheckpoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bguler on 4/25/16.
 */
public class TestBackup {

    private static List<String> backupList = new ArrayList<>();


    public static void main(String[] args) {
        backupList.add("planetlab3.rutgers.edu");
        backupList.add("saturn.planetlab.carleton.ca");
        backupList.add("planetlab01.cs.washington.edu");
        Backup b;
        switch (args[0]) {
            case "full":
                b = new Backup(backupList, FullCheckpoint.class);
                break;
            case "periodic":
                b = new Backup(backupList, PeriodicCheckpoint.class);
                break;
            case "incremental":
                b = new Backup(backupList, IncrementalCheckpoint.class);
                break;
            case "differential":
                b = new Backup(backupList, DifferentialCheckpoint.class);
                break;
        }
    }

}
