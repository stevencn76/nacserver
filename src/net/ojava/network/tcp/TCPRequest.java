package net.ojava.network.tcp;

import java.io.Serializable;
import java.util.UUID;

/*
 * TCPRequest类用于封装一个请求，请求指的是在tcp的客户端与服务端之间发送的异步消息，需要对方给与回答，
 * 当需要向tcp的一端发送一个请求，并等待对方给与回答时，则使用该类封装实例
 * 
 * 作者：陈宝峰
 * 创建日期：2011-07-20
 * 最后修改日期：2011-07-20
 */
public final class TCPRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private Serializable data;
	
	public TCPRequest() {
		this(null);
	}
	
	public TCPRequest(Serializable data) {
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
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("TCPRequest object:[id:");
		sb.append(id);
		sb.append(", data:");
		if(null!=data)
			sb.append(data);
		else
			sb.append("null");
		sb.append("]");
		
		return sb.toString();
	}
}
