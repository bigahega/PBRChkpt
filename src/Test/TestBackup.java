package Test;

import Server.Backup.Backup;
import Server.Shared.Checkpoints.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by bguler on 4/25/16.
 */
public class TestBackup {

    private static List<String> backupList = new ArrayList<>();

    private static <E> List<E> pickNRandomItems(List<E> list, int n, Random r) {
        int length = list.size();

        if (length < n)
            return null;

        for (int i = length - 1; i >= length - n; --i)
            Collections.swap(list, i, r.nextInt(i + 1));

        return list.subList(length - n, length);
    }

    private static <E> List<E> pickNRandomItems(List<E> list, int n) {
        return pickNRandomItems(list, n, ThreadLocalRandom.current());
    }

    public static void main(String[] args) {
        backupList.add("planetlab01.cs.washington.edu");
        backupList.add("planetlab3.rutgers.edu");
        backupList.add("planetlab03.cs.washington.edu");
        backupList.add("planetlab04.cs.washington.edu");
        backupList.add("planetlab02.cs.washington.edu");
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
            case "pincremental":
                b = new Backup(backupList, PeriodicIncrementalCheckpoint.class);
                break;
            case "cpincremental":
                b = new Backup(backupList, CompressedPeriodicIncrementalCheckpoint.class);
                break;
            case "cperiodic":
                b = new Backup(backupList, CompressedPeriodicCheckpoint.class);
                break;
            case "pdifferential":
                b = new Backup(backupList, PeriodicDifferentialCheckpoint.class);
                break;
        }
    }

}
