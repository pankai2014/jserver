package org.kaipan.jserver.database;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.ReadOptions;
import org.iq80.leveldb.Snapshot;
import org.iq80.leveldb.WriteOptions;
import org.kaipan.jserver.socket.log.Logger;

public class LevelDB
{
	private static final JniDBFactory factory = JniDBFactory.factory;
    
    /** level DB instance */
    private DB db = null;
    private Options options = null;
    private File dbFile = null;
    private ReadOptions rdOpt = null;
    private WriteOptions wtOpt = null;
    
    /**
     * Serials of construct method
     * so you can define the DB engine yourself if you know level DB well
     * 
     * @throws IOException 
    */
    public LevelDB(String dbFile) throws IOException
    {
        this(dbFile, getDefaultOptions());
    }
    
    public LevelDB(String dbFile, Options options) throws IOException
    {
        this(dbFile, options, new ReadOptions(), new WriteOptions());
    }
    
    public LevelDB(String dbFile, Options options, 
            ReadOptions rdOpt, WriteOptions wtOpt) throws IOException
    {
        this.dbFile = new File(dbFile);
        this.options = options;
        this.rdOpt = rdOpt;
        this.wtOpt = wtOpt;
        this.db = factory.open(this.dbFile, this.options);
    }
    
    /**
     * default options settings
     * 
     * @return  Options
    */
    private static Options getDefaultOptions()
    {
        Options options = new Options();
        
        options.createIfMissing(true);
        options.errorIfExists(false);
        options.cacheSize(4194304);    
        
        return options;
    }
    
    public byte[] get(byte[] key) 
    {
    	return db.get(key, rdOpt);
    }
    
    public Snapshot set(byte[] key, byte value[]) 
    {
    	return db.put(key, value, wtOpt);
    }
    
    public Snapshot delete(byte[] key) 
    {
    	return db.delete(key, wtOpt);
    }
    
    public String shift()
    {
        String val = null;
        
        DBIterator it = db.iterator(rdOpt);
        it.seekToFirst();
        if ( it.hasNext() ) {
            Entry<byte[], byte[]> e = it.next();
            val = JniDBFactory.asString(e.getValue());
            db.delete(e.getKey(), wtOpt);
        }
        
        try {
            it.close();
        } 
        catch (IOException e) {
            Logger.error(e.getStackTrace());
        }
        
        return val;
    }

    public String pop()
    {
        String val = null;
        
        DBIterator it = db.iterator(rdOpt);
        it.seekToLast();
        if ( it.hasNext() ) {
            Entry<byte[], byte[]> e = it.prev();
            val = JniDBFactory.asString(e.getValue());
            db.delete(e.getKey(), wtOpt);
        }
        
        try {
            it.close();
        } 
        catch (IOException e) {
        	Logger.error(e.getStackTrace());
        }
        
        return val;
    }
    
    public void close()
    {
        try {
            db.close();
        } 
        catch (IOException e) {
        	Logger.warn("Leveldb failed to shut down normally");
        }
    }
}
