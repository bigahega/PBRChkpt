package Server.Shared;

import org.rocksdb.*;

import java.io.Serializable;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 10.03.2016.
 */
public class KeyValueStore implements Serializable {

    private RocksDB rocksDB_instance;
    private Options options;

    public KeyValueStore()
    {
        this.options = new Options();
        this.options.setCreateIfMissing(true);
        try {
            this.rocksDB_instance = RocksDB.open(this.options, "/tmp/pbr_db");
        } catch (RocksDBException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    public void put(String key, String value)
    {
        try {
            this.rocksDB_instance.put(new WriteOptions(), key.getBytes(), value.getBytes());
        } catch (RocksDBException ex) {
            ex.printStackTrace();
        }
    }

    public String get(String key)
    {
        byte[] val_bytes = null;
        try {
            val_bytes = this.rocksDB_instance.get(new ReadOptions(), key.getBytes());
        } catch (RocksDBException ex) {
            ex.printStackTrace();
        }
        return new String(val_bytes);
    }

}
