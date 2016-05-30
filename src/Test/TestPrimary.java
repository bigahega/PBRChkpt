package Test;

import Server.Primary.Primary;
import Server.Shared.Checkpoints.IncrementalCheckpoint;

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
        backupList.add("pl2.sos.info.hiroshima-cu.ac.jp");
        Primary p = new Primary(backupList, IncrementalCheckpoint.class, "/home/ku_distributed/db.txt", 50000);
        //Primary p = new Primary(backupList, PeriodicCheckpoint.class, null, -1);
    }

}
