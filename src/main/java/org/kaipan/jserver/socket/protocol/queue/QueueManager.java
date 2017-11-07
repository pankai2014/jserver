package org.kaipan.jserver.socket.protocol.queue;

import java.io.IOException;

import org.kaipan.jserver.database.LevelDB;
import org.kaipan.jserver.socket.log.Logger;
import org.kaipan.jserver.socket.util.IntegerUtil;

public class QueueManager
{
	private LevelDB leveldb;
	
	private volatile int headIndex;
	private volatile int tailIndex;
	
	private volatile int popCount;
	private volatile int pushCount;
	private volatile int ackCount;
	
    private String headIndexKey;
    private String tailIndexKey;

    private String popCountKey;
    private String pushCountKey;
    private String ackCountKey;
	
	private static QueueManager queue;
	
	private QueueManager(String dbFile) 
	{
		try {
			this.leveldb = new LevelDB(dbFile);
		} 
		catch (IOException e) {
			Logger.error(e.getStackTrace());
		}
		
		this.headIndexKey = "_head_index";
		this.tailIndexKey = "_tail_index";
		
		this.popCountKey  = "_pop_count_index";
		this.pushCountKey = "_push_count_index";
		this.ackCountKey  = "_ack_count_index";
	}
	
	public static QueueManager getInstance(String dbFile) 
	{
		if ( queue == null ) {
			queue = new QueueManager(dbFile);
		}
		
		return queue;
	}
	
	public LevelDB getLevelDB() 
	{
		return leveldb;
	}

	public int getHeadIndex()
	{
		byte[] bytes = this.leveldb.get(this.headIndexKey.getBytes());
		
		headIndex = bytes == null ? 1 : IntegerUtil.bigEndian2Int(bytes);
		return headIndex;
	}

	public void setHeadIndex(int headIndex)
	{
		this.headIndex = headIndex;
		
		leveldb.set(headIndexKey.getBytes(), IntegerUtil.int2BigEndian(headIndex));
	}

	public int getTailIndex()
	{
		byte[] bytes = leveldb.get(tailIndexKey.getBytes());
		
		tailIndex = bytes == null ? 1 : IntegerUtil.bigEndian2Int(bytes);
		return tailIndex;
	}

	public void setTailIndex(int tailIndex)
	{
		this.tailIndex = tailIndex;
		
		leveldb.set(tailIndexKey.getBytes(), IntegerUtil.int2BigEndian(tailIndex));
	}

	public int getPopCount()
	{
		byte[] bytes = leveldb.get(popCountKey.getBytes());
		
		popCount = bytes == null ? 0 : IntegerUtil.bigEndian2Int(bytes);
		return popCount;
	}

	public void setPopCount(int popCount)
	{
		this.popCount = popCount;
		
		leveldb.set(popCountKey.getBytes(), IntegerUtil.int2BigEndian(popCount));
	}

	public int getPushCount()
	{
		byte[] bytes = leveldb.get(pushCountKey.getBytes());
		
		pushCount = bytes == null ? 0 : IntegerUtil.bigEndian2Int(bytes);
		return pushCount;
	}

	public void setPushCount(int pushCount)
	{
		this.pushCount = pushCount;
		
		leveldb.set(pushCountKey.getBytes(), IntegerUtil.int2BigEndian(pushCount));
	}

	public int getAckCount()
	{
		byte[] bytes = leveldb.get(ackCountKey.getBytes());
		
		ackCount = bytes == null ? 0 : IntegerUtil.bigEndian2Int(bytes);
		return ackCount;
	}

	public void setAckCount(int ackCount)
	{
		this.ackCount = ackCount;
		
		leveldb.set(ackCountKey.getBytes(), IntegerUtil.int2BigEndian(ackCount));
	}

	public String getHeadIndexKey()
	{
		return headIndexKey;
	}

	public void setHeadIndexKey(String headIndexKey)
	{
		this.headIndexKey = headIndexKey;
	}

	public String getTailIndexKey()
	{
		return tailIndexKey;
	}

	public void setTailIndexKey(String tailIndexKey)
	{
		this.tailIndexKey = tailIndexKey;
	}

	public String getPopCountKey()
	{
		return popCountKey;
	}

	public void setPopCountKey(String popCountKey)
	{
		this.popCountKey = popCountKey;
	}

	public String getPushCountKey()
	{
		return pushCountKey;
	}

	public void setPushCountKey(String pushCountKey)
	{
		this.pushCountKey = pushCountKey;
	}

	public String getAckCountKey()
	{
		return ackCountKey;
	}

	public void setAckCountKey(String ackCountKey)
	{
		this.ackCountKey = ackCountKey;
	}
}
