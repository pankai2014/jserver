package org.kaipan.jserver.socket.protocol.queue;

import org.kaipan.jserver.socket.util.IntegerUtil;

public class PushAction implements Action
{
	public byte[] process(QueueManager manager, QueueBean bean)
	{
		long tailIndex = manager.getTailIndex();
		long pushCount = manager.getPushCount();
		
		boolean written = manager.set(IntegerUtil.long2BigEndian(tailIndex), bean.getData());
		if ( written == false ) {
			return null;
		}
		
		manager.setTailIndex(++tailIndex);
		manager.setPushCount(++pushCount);
		
		return Action.OK_MSG;
	}
}
