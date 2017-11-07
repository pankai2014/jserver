package org.kaipan.jserver.socket.protocol.queue;

public class ActionFactory
{
	public static Action create(int type) 
	{
		switch ( type ) {
			case QueueBean.QUEUE_PUSH_TYPE:
				return new PushAction();
				
			case QueueBean.QUEUE_POP_TYPE:
				return new PopAction();
				
			case QueueBean.QUEUE_ACK_TYPE:
				return new AckAction();
				
			case QueueBean.QUEUE_STATS_TYPE:
				return new StatsAction();
		}
		
		throw new IllegalArgumentException("Incorrect type value");
	}
}
