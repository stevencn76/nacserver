package net.ojava.network.telnet;

/**
 * telnet命令处理器的接口规范
 * @author chenbaofeng
 *
 */
public interface ITelnetCommandProcessor {
	/**
	 * 获取命令处理模块的功能描述
	 * @return 返回描述字符串
	 */
	public String getDescription();
	
	/**
	 * 获取命令处理模块的帮助信息，包含模块可以处理的所有命令和说明
	 * @return 返回信息字符串
	 */
	public String help();
	
	/**
	 * 处理命令
	 * @param args 命令信息，包含参数信息，以数组方式呈现，数组的第一个元素是命令名称
	 * @return 命令处理结果的字符串
	 */
	public String process(String[] args);
}
