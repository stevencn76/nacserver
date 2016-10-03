package net.ojava.network.tcp;

/*
 * TCPNetClientListener是一个监听器的接口规范，你可以遵守该规范实现自定义的类，通过向TCPNetClient
 * 注册该接口的相关实例对象类监听客户端端的事件
 * 
 * 作者：陈宝峰
 * 创建日期：2011-07-20
 * 最后修改日期：2011-07-20
 */
public interface TCPNetClientListener {
	public void onMessage(final TCPMessage message); //收到服务器发来的message
	public void onRequest(final TCPRequest request, final TCPResponse response); //收到服务器发来的request
	public void onClosed(); //服务器关闭了连接
	public void onInterrupt(String ip,String port); //网络发生中断，正常为心跳包发送失败时的诊断
	public void onUnknowData(final String msg); //收到服务器发来的数据为无法识别的对象时
}
