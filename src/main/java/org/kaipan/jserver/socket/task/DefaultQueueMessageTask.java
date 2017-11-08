package org.kaipan.jserver.socket.task;

import org.kaipan.jserver.socket.core.Message;
import org.kaipan.jserver.socket.core.Server;
import org.kaipan.jserver.socket.core.Socket;
import org.kaipan.jserver.socket.protocol.queue.Action;
import org.kaipan.jserver.socket.protocol.queue.ActionFactory;
import org.kaipan.jserver.socket.protocol.queue.QueueBean;
import org.kaipan.jserver.socket.protocol.queue.QueueManager;
import org.kaipan.jserver.socket.protocol.queue.QueueUtil;
import org.kaipan.jserver.socket.protocol.websocket.WsFrame;
import org.kaipan.jserver.socket.protocol.websocket.WsUtil;

public class DefaultQueueMessageTask extends WsMessageTask
{
	private static Object LOCK = new Object();
	
	public DefaultQueueMessageTask(Server server, Socket socket, Message message)
	{
		super(server, socket, message);
	}

	@Override
	protected void onMessage(WsFrame request)
	{
		QueueManager manager = QueueManager.getInstance(getWsConfig().leveldbPath());
		if ( manager == null ) {
			send(request.getSocketId(), WsUtil.newCloseFrame(WsFrame.CLOSE_SERVER_ERROR));
		}
		
		QueueBean  bean  = QueueUtil.parseBean(request.getData());
		Action processor = ActionFactory.create(bean.getType());
		
		byte[] result = null;
		
		synchronized ( LOCK ) {
			result = processor.process(manager, bean);
			if ( result == null ) {
				send(request.getSocketId(), WsUtil.newCloseFrame(WsFrame.CLOSE_ABNORMAL));
			}
			
			LOCK.notify();
		}
		
		byte[] response = WsUtil.newFrame(WsFrame.OPCODE_BINARY, 
				false, result, true);
		
		send(request.getSocketId(), response);
	}
}
