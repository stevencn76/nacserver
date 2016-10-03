package net.ojava.network.telnet;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Telnet服务器与客户端的会话类
 * @author chenbaofeng
 *
 */
public class TelnetServerSession implements Runnable, ITelnetSessionContext {
	private static Logger log = Logger.getLogger(TelnetServerSession.class);
	
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	
	private Set<ITelnetServerSessionListener> listeners = new HashSet<ITelnetServerSessionListener>();
	private Thread inputThread;

	private String remoteAddress;
	private int remotePort;
	private String localAddress;
	private int localPort;
	
	private Hashtable<String, Object> attributes = new Hashtable<String, Object>();
	
	public TelnetServerSession(Socket socket) {
		this.socket = socket;
		
		remoteAddress = socket.getInetAddress().getHostAddress();
		remotePort = socket.getPort();
		localAddress = socket.getLocalAddress().getHostAddress();
		localPort = socket.getLocalPort();
	}
	
	/**
	 * 注册session listener
	 * @param listener
	 */
	public void addSessionListener(ITelnetServerSessionListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * 删除注册的session listener
	 * @param listener
	 */
	public void removeSessionListener(ITelnetServerSessionListener listener) {
		listeners.remove(listener);
	}
	
	private BufferedReader getReader() throws Exception {
		if(reader == null) {
			InputStream is = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			reader = new BufferedReader(isr);
		}
		
		return reader;
	}
	
	private PrintWriter getWriter() throws Exception {
		if(writer == null) {
			OutputStream os = socket.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os);
			writer = new PrintWriter(osw);
		}
		
		return writer;
	}
	
	/**
	 * 开启与客户端的会话服务
	 */
	public void start() {
		if(inputThread == null) {
			inputThread = new Thread(this);
			inputThread.setName("telnet_server_session_thread");
			inputThread.start();
		}
	}
	
	/**
	 * 停止与客户端的会话服务，并关闭网络连接
	 */
	public void stop() {
		try {
			socket.close();
		} catch (Exception e){
		}
	}

	@Override
	public void run() {
		String line;
		
		try {
			sendWelcome();
			sendCommandTip();
			
			while((line = getReader().readLine()) != null) {
				line = line.trim();
				
				if(line.length() == 0) {
					sendCommandTip();
				}
				else 	if(line.equalsIgnoreCase("bye")) {
					break;
				}
				else {
					for(ITelnetServerSessionListener listener : listeners) {
						listener.commandArrived(this, line);
					}
				}
			}
		} catch (Exception e) {
			log.error("session net error", e);
		}
		
		try {
			if(!socket.isClosed())
				socket.close();
		} catch (Exception e){}
		
		for(ITelnetServerSessionListener listener : listeners) {
			listener.sessionStoped(this);
		}
	}

	@Override
	public void setAttribute(String key, Object value) {
		attributes.put(key, value);
	}

	@Override
	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	@Override
	public void removeAttribute(String key) {
		attributes.remove(key);
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public String getLocalAddress() {
		return localAddress;
	}

	public int getLocalPort() {
		return localPort;
	}
	
	public synchronized void sendCommandTip() throws Exception {
		getWriter().write("telnet server>");
		getWriter().flush();
	}
	
	public synchronized void sendWelcome() throws Exception {
		getWriter().write("Welcome to ojava telnet server, version is ");
		getWriter().write(TelnetServer.VERSION);
		getWriter().write("\r\n");
		getWriter().flush();
	}

	@Override
	public synchronized void sendResponse(Object response, boolean isEnd) throws Exception {
		if(response instanceof Throwable) {
			Throwable t = (Throwable)response;
			t.printStackTrace(getWriter());
			getWriter().write("\r\n");
		}
		else {
			getWriter().write(response.toString());
			getWriter().write("\r\n");
		}
		
		if(isEnd) {
			sendCommandTip();
		}

		getWriter().flush();
	}
}
