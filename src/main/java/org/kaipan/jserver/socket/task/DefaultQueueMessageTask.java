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
	public DefaultQueueMessageTask(Server server, Socket socket, Message message)
	{
		super(server, socket, message);
	}

	@Override
	protected void onMessage(WsFrame frame)
	{
		QueueManager manager = QueueManager.getInstance(getWsConfig().queuePath());
		if ( manager == null ) {
			send(frame.getSocketId(), WsUtil.newCloseFrame(WsFrame.CLOSE_SERVER_ERROR));
		}
		
		QueueBean bean = QueueUtil.parseBean(frame.getData());
		
		Action processor = ActionFactory.create(bean.getType());
		byte[] data = processor.process(manager, bean);
		if ( data == null ) {
			send(frame.getSocketId(), WsUtil.newCloseFrame(WsFrame.CLOSE_ABNORMAL));
		}
		
		byte[] result = WsUtil.newFrame(WsFrame.OPCODE_BINARY, 
				false, data, true);
		
		send(frame.getSocketId(), result);
	}
}
