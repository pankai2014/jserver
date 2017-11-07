package org.kaipan.jserver.socket.protocol.queue;

import org.kaipan.jserver.socket.util.IntegerUtil;

public class PopAction implements Action
{
	public byte[] process(QueueManager manager, QueueBean bean)
	{
		long headIndex = manager.getHeadIndex();
		long tailIndex = manager.getTailIndex();
		long popCount  = manager.getPopCount();
		
		if ( headIndex >= tailIndex ) {
			return Action.EMPTY_MSG;
		}
		
		byte[] value = manager.get(IntegerUtil.long2BigEndian(headIndex));
		if ( value == null ) {
			return Action.EMPTY_MSG;
		}
		
		manager.setHeadIndex(++headIndex);
		manager.setPopCount(++popCount);
		
		byte[] result = new byte[2 + value.length];
		
		result[1] = 000;
		result[0] = Action.OK;
		
		System.arraycopy(value, 0, result, 2, value.length);
		
		return result;
	}
}
