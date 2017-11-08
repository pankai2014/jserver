package org.kaipan.jserver.socket.protocol.queue;

import org.kaipan.jserver.socket.util.IntegerUtil;

public class QueueUtil
{
	public static QueueBean parseBean(byte[] result) 
	{
		QueueBean bean = new QueueBean();
		
		bean.setId(IntegerUtil.bigEndian2Int(result, 0));
		bean.setType(IntegerUtil.bigEndian2Int(result, 4));
		
		byte[] data = null;
		
		int length = result.length - 8;
		if ( length > 0 ) {
			data = new byte[length];
		}
		
		if ( data != null ) {
			System.arraycopy(result, 8, data, 0, length);
			bean.setData(data);
		}
		
		return bean;
	}
}
