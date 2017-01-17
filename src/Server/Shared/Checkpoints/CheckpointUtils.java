package Server.Shared.Checkpoints;

import com.github.luben.zstd.Zstd;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

import java.io.*;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Berkin GÜLER on 13.08.2016.
 */
public class CheckpointUtils {

    public static byte[] byteArrayToGZIPByteArray(byte[] in) {
        byte[] compressedBytes = null;

        Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION);
        compressor.setInput(in);
        compressor.finish();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(in.length)) {
            byte[] buf = new byte[1024];
            while (!compressor.finished()) {
                int count = compressor.deflate(buf);
                baos.write(buf, 0, count);
            }
            compressedBytes = baos.toByteArray();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return compressedBytes;
    }

    public static byte[] mapToLZMA2ByteArray(Map<String, String> map) {
        byte[] result = null;
        byte[] data = mapToByteArray(map);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XZOutputStream xzOutputStream = new XZOutputStream(baos, new LZMA2Options(LZMA2Options.PRESET_MAX));
            xzOutputStream.write(data);
            xzOutputStream.close();
            result = baos.toByteArray();
            System.out.println("Input length: " + data.length + " Output length: " + result.length);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return result;
    }

    public static byte[] byteArrayToLZMA2ByteArray(byte[] data) {
        byte[] result = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XZOutputStream xzOutputStream = new XZOutputStream(baos, new LZMA2Options(LZMA2Options.PRESET_MAX));
            xzOutputStream.write(data);
            xzOutputStream.close();
            result = baos.toByteArray();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return result;
    }

    public static byte[] byteArrayToZstdByteArray(byte[] data) {
        return Zstd.compress(data);
    }

    public static byte[] mapToGZIPByteArray(Map<String, String> map) {
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

    public static byte[] GZIPcompressByteArray(byte[] bytes) {
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
    public static Map<String, String> GZIPcompressedByteArrayToMap(byte[] compressedByteArray) {
        Map<String, String> result = null;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(compressedByteArray);
             GZIPInputStream gis = new GZIPInputStream(bais);
             ObjectInputStream ois = new ObjectInputStream(gis)) {

            Object incomingObj = ois.readObject();
            if (incomingObj instanceof Map)
                result = (Map<String, String>) incomingObj;
            else
                throw new Exception("Incoming object is not a Map<String,String>");

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> byteArrayToMap(byte[] byteArray) {
        Map<String, String> result = null;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
            ObjectInputStream ois = new ObjectInputStream(bais);) {
            Object incomingObj = ois.readObject();
            if(incomingObj instanceof Map)
                result = (Map<String, String>) incomingObj;
            else
                throw new Exception("Incoming object is not a Map<String, String>");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

    public static byte[] mapToByteArray(Map<String, String> map) {
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
