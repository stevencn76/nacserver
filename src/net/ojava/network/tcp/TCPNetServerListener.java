package net.ojava.network.tcp;

/*
 * TCPNetServerListener是一个监听器的接口规范，你可以遵守该规范实现自定义的类，通过向TCPNetServer
 * 注册该接口的相关实例对象类监听服务端的事件
 * 
 * 作者：陈宝峰
 * 创建日期：2011-07-20
 * 最后修改日期：2011-07-20
 */
public interface TCPNetServerListener {
	/*
	 * 当有一个客户端成功连接时，该事件会触发，参数中的TCPNetSessionContext实例唯一对应于该客户端
	 */
	public void onConnected(final TCPNetSessionContext context);

	/*
	 * 当与一个客户断开连接时，该事件会触发，参数中的TCPNetSessionContext实例唯一对应于该客户端
	 */
	public void onClosed(final TCPNetSessionContext context);

	/*
	 * 当客户端发来一个message时，该事件会触发，参数中的TCPNetSessionContext实例唯一对应于该客户端
	 */
	public void onMessage(final TCPNetSessionContext context, final TCPMessage message); //收到客户端发来的message

	/*
	 * 当客户端发来一个request时，该事件会触发，参数中的TCPNetSessionContext实例唯一对应于该客户端
	 * 在该方法的实现中处理事件，当处理完成后将需要返回的结果设置到参数TCPResponse的实例中
	 */
	public void onRequest(final TCPNetSessionContext context, final TCPRequest request, final TCPResponse response); //收到客户端发来的request
}
