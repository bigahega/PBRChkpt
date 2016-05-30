//package Server.Shared;
//
//import Server.Backup.Backup;
//import Server.Primary.Primary;
//import org.apache.commons.io.FilenameUtils;
//
//import java.io.File;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.List;
//
///**
// * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 08.03.2016.
// */
//public class ServerMain {
//
//    private static Server server;
//
//    public static void main(String[] args) {
//        if(args.length >= 1) {
//            System.out.println("Not enough arguments supplied.");
//            System.exit(-1);
//        }
//
//        switch (args[0]) {
//            case "--primary":
//                try {
//                    File backups = new File(args[1]);
//                    if(!backups.isFile() || backups.isDirectory() || !backups.canRead() ||
//                            !FilenameUtils.isExtension(args[1], "txt")) {
//                        System.out.println("Second parameter should be a valid .txt file that contains backup replica" +
//                                " hosts");
//                        System.exit(-1);
//                    }
//                    List<String> backupServers = Files.readAllLines(Paths.get(args[1]));
//                    server = new Primary(backupServers);
//                }
//                catch (Exception ex)
//                {
//                    ex.printStackTrace();
//                    System.exit(-1);
//                }
//                break;
//            case "--backup":
//                server = new Backup(args[1]);
//                break;
//            default:
//                System.out.println("First argument has to be --primary or --backup.");
//                System.exit(-1);
//        }
//
//
//    }
//
//}
