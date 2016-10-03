package net.ojava.network.tcp;

import java.net.Socket;

/*
 * 每当一个客户端(TCPNetClient)连接到服务端之后，在服务端会产生一个TCPNetSessionContext的唯一实例
 * 该实例唯一对应着成功连接的客户端，可以使用该实例向客户端发送request和message
 * 同时TCPNetSessionContext给用户提供了存储自定义信息的接口
 * 
 * 作者：陈宝峰
 * 创建日期：2011-07-20
 * 最后修改日期：2011-07-20
 */
public interface TCPNetSessionContext {
	/*
	 * 用户向实例中存储自定义的信息，信息内容为键值对
	 */
	public void setAttribute(Object key, Object value);

	/*
	 * 根据键删除实例中存储的自定义信息
	 */
	public void removeAttribute(Object key);

	/*
	 * 根据键提取实例中存储的自定义信息
	 */
	public Object getAttribute(Object key);

	/*
	 * 向客户端发送一个message
	 */
	public void postMessage(TCPMessage message) throws Exception;
	
	/*
	 * 向客户端发送一个request
	 */
	public TCPResponse sendRequest(TCPRequest request) throws Exception;

	/*
	 * 向客户端发送一个request，并设置等待结果的超时时间
	 */
	public TCPResponse sendRequest(TCPRequest request, long timeout) throws Exception;

	/*
	 * 取得当前连接着的客户端的IP地址
	 */
	public String getRemoteAddress();

	/*
	 * 取得当前连接着的客户端的tcp端口信息
	 */
	public int getRemotePort();
	
	/*
	 * 取得当前与客户端连接用的服务端IP地址
	 */
	public String getLocalAddress();

	/*
	 * 取得当前与客户端连接用的服务端端口
	 */
	public int getLocalPort();
	
	/*
	 *取得当前与客户端连接的服务端socket 
	 */
	public Socket getSocket();
	
	/*
	 * 判断当前TCP连接是否活着
	 */
	public boolean isConnected();
}
