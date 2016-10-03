package net.ojava.network.tcp;

import java.io.Serializable;
import java.util.UUID;

/*
 * TCPMessage类用于封装一个消息，消息指的是在tcp的客户端与服务端之间发送的异步消息，不需要对方给与任何回答，
 * 当需要向tcp的一端发送一个不需要回答的消息时，则使用该类封装实例
 * 
 * 作者：陈宝峰
 * 创建日期：2011-07-20
 * 最后修改日期：2011-07-20
 */
public final class TCPMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private Serializable data;
	
	public TCPMessage() {
		this(null);
	}
	
	public TCPMessage(Serializable data) {
		id = UUID.randomUUID().toString();
		this.data = data;
	}
	
	public String getId() {
		return id;
	}

	public void setData(Serializable data) {
		this.data = data;
	}

	public Serializable getData() {
		return data;
	}
}
