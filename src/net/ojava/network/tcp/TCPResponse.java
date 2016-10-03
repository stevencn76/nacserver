package net.ojava.network.tcp;

import java.io.Serializable;

/*
 * TCPResponse类用于封装一个针对请求的响应，当通过tcp向一方发送请求后，对方需要回复一个
 * TCPResponse的对象以进行响应，注意，该对象的ID和对应的TCPRequest实例中的ID相同
 * 你不需要使用该类创建实例，当使用sendRequest方法使用，方法的返回值即为该类的实例
 * 
 * 作者：陈宝峰
 * 创建日期：2011-07-20
 * 最后修改日期：2011-07-20
 */
public final class TCPResponse implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private Serializable data;
	
	public TCPResponse(String id, Serializable data) {
		this.id = id;
		this.data = data;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public void setData(Serializable data) {
		this.data = data;
	}

	public Serializable getData() {
		return data;
	}
}
