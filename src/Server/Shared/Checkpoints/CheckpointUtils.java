package Server.Shared.Checkpoints;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Berkin GÜLER on 13.08.2016.
 */
public class CheckpointUtils {

    public static byte[] mapToCompressedByteArray(Map<String, Map<String, String>> map) {
        byte[] result = null;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream goz = new GZIPOutputStream(baos);
             ObjectOutputStream oos = new ObjectOutputStream(goz);) {

            oos.writeObject(map);
            result = baos.toByteArray();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return result;
    }

    public static byte[] compressByteArray(byte[] bytes) {
        byte[] result = null;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream goz = new GZIPOutputStream(baos);) {
            goz.write(bytes);
            result = baos.toByteArray();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Map<String, String>> compressedByteArrayToMap(byte[] compressedByteArray) {
        Map<String, Map<String, String>> result = null;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(compressedByteArray);
             GZIPInputStream gis = new GZIPInputStream(bais);
             ObjectInputStream ois = new ObjectInputStream(gis)) {

            Object incomingObj = ois.readObject();
            if (incomingObj instanceof Map)
                result = (Map<String, Map<String, String>>) incomingObj;
            else
                throw new Exception("Incoming object is not a Map<String,String>");

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Map<String, String>> byteArrayToMap(byte[] byteArray) {
        if (byteArray == null || byteArray.length == 0)
            return new HashMap<>();
        Map<String, Map<String, String>> result = null;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
            ObjectInputStream ois = new ObjectInputStream(bais);) {
            Object incomingObj = ois.readObject();
            if(incomingObj instanceof Map)
                result = (Map<String, Map<String, String>>) incomingObj;
            else
                throw new Exception("Incoming object is not a Map<String, String>");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

    public static byte[] mapToByteArray(Map<String, Map<String, String>> map) {
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
