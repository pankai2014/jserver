package org.kaipan.jserver.socket.protocol.queue;

import org.kaipan.jserver.socket.util.IntegerUtil;

public class QueueUtil
{
	public static QueueBean parseBean(byte[] data) 
	{
		QueueBean bean = new QueueBean();
		
		bean.setId(IntegerUtil.bigEndian2Int(data, 0, 4));
		bean.setType(IntegerUtil.bigEndian2Int(data, 4, 4));
		
		byte[] body = null;
		
		int length = data.length - 8;
		if ( length > 0 ) {
			body = new byte[length];
		}
		
		if ( body != null ) {
			System.arraycopy(data, 8, body, 0, length);
			bean.setData(body);
		}
		
		return bean;
	}
}
