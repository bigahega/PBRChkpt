package Server.Shared.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 10.08.2016.
 */
public class Log {

    private PrintWriter logWriter;

    public Log(String logFilePath) {
        try {
            this.logWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFilePath, true)));
        } catch (IOException ex) {
            System.out.println("!!! Logging failed !!!");
        }
    }

    public void write(String log) {
        this.logWriter.println(log);
    }

    public void close() {
        this.logWriter.close();
    }
}
