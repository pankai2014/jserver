package org.kaipan.jserver.socket.protocol.queue;

public class AckAction implements Action 
{
	public byte[] process(QueueManager manager, QueueBean bean)
	{
		long ackCount = manager.getAckCount();
		
		boolean written = manager.delete(bean.getData());
		if ( written == false ) {
			return null;
		}
		
		manager.setAckCount(++ackCount);
		
		return Action.OK_MSG;
	}
}
