package Test;

import Server.Backup.Backup;
import Server.Shared.Checkpoints.IncrementalCheckpoint;

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
        backupList.add("pl2.sos.info.hiroshima-cu.ac.jp");
        Backup b = new Backup(backupList, IncrementalCheckpoint.class);
    }

}
