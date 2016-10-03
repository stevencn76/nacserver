package net.ojava.network.telnet;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * Telnet服务器类，使用方法：
 * 1. Telnetserver server = new TelnetServer(1099); //创建Telnet服务对象，参数中制定telent的监听端口
 * 2. server.addServerListener(processor); //将命令处理器s对象的实例注册到服务对象中
 * 3. server.start(); //启动Telnet服务
 * 
 * 使用结束时需要停止服务：
 * 4. server.stop(); //停止服务
 * @author chenbaofeng
 *
 */
public class TelnetServer implements Runnable, ITelnetServerSessionListener {
	private static Logger log = Logger.getLogger(TelnetServer.class);
	public static final String VERSION = "1.0.0";
	
	private ServerSocket serverSocket;
	private int port;
	
	/**监听客户端连接的线程对象*/
	private Thread acceptThread = null;
	/**监听连接的线程是否需要停止*/
	private boolean acceptThreadStop = false;
	
	/**记录所有当前连接中的客户端会话对象的集合*/
	private Set<TelnetServerSession> sessions = new HashSet<TelnetServerSession>();
	
	/**记录所有用于处理telnet命令的处理器对象的集合*/
	private Set<ITelnetCommandProcessor> processors = new HashSet<ITelnetCommandProcessor>();
	
	/**
	 * 将数据结果发送到客户端的线程，为了防止网络阻塞导致发送者阻塞，所以将所有发送结果缓存，
	 * 然后通过该线程逐条发送
	 * @author chenbaofeng
	 *
	 */
	private class SendThread implements Runnable {
		private List<Object> responseCache = new LinkedList<Object>();
		private Thread t;
		private boolean stoped = false;

		public SendThread() {
		}
		
		public void start() {
			if(t == null) {
				t = new Thread(this);
				t.setName("telnet_server_send_response_thread");
				t.setDaemon(true);
				t.start();
			}
		}
		
		public void stop() {
			stoped = true;
			synchronized(responseCache) {
				try {
					responseCache.notify();
				} catch (Exception e){}
			}
		}
		
		@Override
		public void run() {
			Object res = null;
			while((res = nextResponse()) != null) {
				doSendResponse(res);
			}
		}
		
		private void appendResponse(Object res) {
			synchronized(responseCache) {
				responseCache.add(res);
				
				try {
					responseCache.notify();
				} catch (Exception e){}
			}
		}
		
		private Object nextResponse() {
			if(stoped)
				return null;
			
			Object res = null;
			
			synchronized(responseCache) {
				while(!stoped && responseCache.size() == 0) {
					try {
						responseCache.wait();
					} catch (Exception e){}
				}
				
				if(!stoped)
					res = responseCache.remove(0);
			}
			
			return res;
		}
	}
	
	private SendThread sendThread;
	
	public TelnetServer(int port) {
		this.port = port;
		
		processors.add(new DefaultTelnetCommandProcessor(this));
	}
	
	/**
	 * 添加命令处理器实例对象
	 * @param listener
	 */
	public void addCommandProcessor(ITelnetCommandProcessor processor) {
		processors.add(processor);
	}
	
	/**
	 * 删除命令处理器实例对象
	 */
	public void removeCommandProcessor(ITelnetCommandProcessor processor) {
		processors.remove(processor);
	}
	
	public Set<TelnetServerSession>getSessions() {
		return sessions;
	}
	
	/**
	 * 启动TelnetServer实例的监听线程
	 * @throws Exception 打开监听端口失败抛出Exception
	 */
	public void start() throws Exception {
		serverSocket = new ServerSocket(port);
		
		acceptThreadStop = false;
		acceptThread = new Thread(this);
		acceptThread.setName(MessageFormat.format("telnet_accept_thread_{0}", port));
		acceptThread.start();
		
		sendThread = new SendThread();
		sendThread.start();
	}
	
	/**
	 * 关闭TelnetServer实例的监听线程
	 * @throws Exception 关闭ServerSocket错误时抛出Exception
	 */
	public void stop() throws Exception {
		sendThread.stop();
		
		acceptThreadStop = true;
		if(serverSocket != null)
			serverSocket.close();
		
		synchronized(sessions) {
			for(TelnetServerSession session : sessions) {
				try {
					session.stop();
				} catch (Exception e) {
				}
			}
			
			sessions.clear();
		}
	}

	@Override
	public void run() {
		while(!acceptThreadStop) {
			try {
				Socket socket = serverSocket.accept();
				TelnetServerSession session = new TelnetServerSession(socket);
				
				synchronized(sessions) {
					sessions.add(session);
				}
				
				session.addSessionListener(this);
				session.start();
			} catch (IOException e) {
				if(!acceptThreadStop)
					log.debug("telnet server accept thread run error", e);
			}
		}
	}
	
	private void doSendResponse(Object response) {
		List<TelnetServerSession>invalidateSessions = new LinkedList<TelnetServerSession>();
		synchronized(sessions) {
			for(TelnetServerSession session : sessions) {
				try {
					session.sendResponse(response, true);
				} catch (Exception e) {
					log.error("send response failed", e);
					log.info("close telnet server session for failed to send response");
					
					invalidateSessions.add(session);
				}
			}
		}
		
		for(TelnetServerSession session : invalidateSessions) {
			try {
				session.stop();
			} catch (Exception e) {
			}
		}
		invalidateSessions.clear();
	}
	
	/**
	 * 向所有会话实例连接的客户端广播响应信息，如果某个会话发送失败，则断开该会话
	 * @param response
	 */
	public void sendResponse(Object response) {
		sendThread.appendResponse(response);
	}

	/**
	 * sessin的会话处理，当session停止服务时，该事件方法会被执行
	 */
	@Override
	public void sessionStoped(TelnetServerSession session) {
		synchronized(sessions) {
			try {
				sessions.remove(session);
			} catch (Exception e){
			}
		}
	}

	@Override
	public void commandArrived(TelnetServerSession session, String command) {
		String [] ss = command.split(" ");
		String [] args = null;
		
		Vector<String> buf = new Vector<String>();
		if(ss != null) {
			for(String ts : ss) {
				if(ts != null)
					ts = ts.trim();
				if(ts != null && ts.length() != 0) {
					buf.add(ts);
				}
			}
			
			if(buf.size() == 0)
				args = null;
			else if(buf.size() == ss.length)
				args = ss;
			else {
				args = new String[buf.size()];
				for(int i=0; i<buf.size(); i++)
					args[i] = buf.get(i);
			}
		}
		
		if(args == null || args.length == 0) {
				try {
					session.sendCommandTip();
				} catch (Exception e) {
					log.error("send response error", e);
				}
				return;
		}
		
		if(args[0].equalsIgnoreCase("help")) {
			int count = processors.size();
			int counter = 0;
			for(ITelnetCommandProcessor processor : processors) {
				counter ++;
				String res = processor.help();
				if(res != null) {
					try {
						session.sendResponse(res, counter>=count);
					} catch (Exception e) {
						log.error("send response error", e);
					}
				}
			}
		}
		else {
			boolean done = false;
			for(ITelnetCommandProcessor processor : processors) {
				String res = processor.process(args);
				if(res != null) {
					try {
						session.sendResponse(res, true);
					} catch (Exception e) {
						log.error("send response error", e);
					}
					
					done = true;
					break;
				}
			}
			
			if(!done) {
				try {
					session.sendResponse("unknown command", true);
//					session.sendCommandTip();
				} catch (Exception e) {
					log.error("send response error", e);
				}
			}
		}
	}
}
