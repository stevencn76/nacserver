package net.ojava.network.telnet;

/**
 * Telnet会话实例的接口规范
 * @author root
 *
 */
public interface ITelnetSessionContext {
	/**
	 * 在会话实例中存放数据(键值对)
	 * @param key 数据对象的名称
	 * @param value 具体的数据内容
	 */
	public void setAttribute(String key, Object value);
	
	/**
	 * 提取存储在会话实例中的数据
	 * @param key 数据对象的名称
	 * @return 返回数据对象，如果不存在则返回null
	 */
	public Object getAttribute(String key);
	
	/**
	 * 删除会话实例中存储的数据
	 * @param key 数据对象的名称s
	 */
	public void removeAttribute(String key);
	
	/**
	 * 向会话实例所连接的客户端发送数据
	 * @param response
	 */
	public void sendResponse(Object response, boolean isEnd) throws Exception;
}
