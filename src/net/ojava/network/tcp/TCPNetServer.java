package net.ojava.network.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.apache.log4j.Logger;

/*
 * TCPNetServer可以用于监听TCPNetClient的连接和与TCPNetClient进行连接和传送数据
 * 该类的使用步骤为：
 * 1. 创建该类的实例
 * 2. 注册监听器(TCPNetServerListener实例)
 * 3. 使用start方法启动服务端监听
 * 4. 使用stop方法停止服务端监听
 * 
 * 作者：陈宝峰
 * 创建日期：2011-07-20
 * 最后修改日期：2011-11-25
 * 修改日志：
 * 2011-11-25 第一次修改 增加处理TCPRequest的耗时日志
 */
public class TCPNetServer implements Runnable {
	private static Logger log = Logger.getLogger(TCPNetServer.class);
	private static final int defaultResponseTimeout = 60000;
	private static final int defaultHeartbeatSpaceTime = 10000;
	private static final int defaultHeartbeatResponseTimeout = 10000;
	
	private ServerSocket serverSocket;
	private boolean stop = false;
	private boolean alive = false;
	
	private HashSet<TCPNetSessionProcessor> processors = new HashSet<TCPNetSessionProcessor>();
	private HashSet<TCPNetServerListener> listeners = new HashSet<TCPNetServerListener>();
	
	private class TCPNetSessionProcessor implements TCPNetSessionContext, Runnable {
		private boolean connected = true;
		
		private Socket socket;
		private ObjectInputStream in;
		private ObjectOutputStream out;
		
		private String remoteAddress;
		private int remotePort;
		private String localAddress;
		private int localPort;
		
		private String buildNetLog(String content) {
			return MessageFormat.format("[remote ip: {0}, remote port: {1}] {2}", getRemoteAddress(), getRemotePort(), content);
		}
		
		private class RequestResult{
			public TCPResponse response;
			
			public RequestResult() {
				response = null;
			}
		}
		private Hashtable<String, RequestResult> resultCache = new Hashtable<String, RequestResult>();

		@Override
		public String getRemoteAddress() {
			return remoteAddress;
		}

		@Override
		public int getRemotePort() {
			return remotePort;
		}

		@Override
		public String getLocalAddress() {
			return localAddress;
		}

		@Override
		public int getLocalPort() {
			return localPort;
		}
		
		@Override
		public Socket getSocket() {
			return socket;
		}
		private Thread thread;
		private boolean stop = false;
		private Hashtable<Object, Object> attributes = new Hashtable<Object, Object>();
		
		private class HeartbeatThread extends Thread {
			@Override
			public void run() {
				while(!stop) {
					try {
						Thread.sleep(defaultHeartbeatSpaceTime);
					}
					catch(Exception e){
						log.debug(buildNetLog("exception"), e);
					}
					
					try {
						sendRequest(new TCPRequest(new TCPHeartbeat()), defaultHeartbeatResponseTimeout);
					}
					catch(Exception e) {
						if(!stop) {
							log.debug(buildNetLog("Heartbeat request failed"), e);
							TCPNetSessionProcessor.this.stop();
							break;
						}
					}
				}
			}
		}
		
		public TCPNetSessionProcessor(Socket socket) {
			this.socket = socket;
			
			remoteAddress = socket.getInetAddress().getHostAddress();
			remotePort = socket.getPort();
			localAddress = socket.getLocalAddress().getHostAddress();
			localPort = socket.getLocalPort();
		}
		

		private final Object getInLock = new Object();
		private  ObjectInputStream getIn() throws Exception {
			synchronized(getInLock) {
				if(in == null) {
					in = new ObjectInputStream(socket.getInputStream());
				}
			}
			return in;
		}
		

		private final Object getOutLock = new Object();
		private  ObjectOutputStream getOut() throws Exception {
			synchronized(getOutLock) {
				if(out == null) {
					out = new ObjectOutputStream(socket.getOutputStream());
				}
			}
			return out;
		}

		@Override
		public void setAttribute(Object key, Object value) {
			attributes.put(key, value);
		}

		@Override
		public void removeAttribute(Object key) {
			attributes.remove(key);
		}

		@Override
		public Object getAttribute(Object key) {
			return attributes.get(key);
		}

		@Override
		public void postMessage(TCPMessage message) throws Exception {
			writeObject(message);
		}
		
		private synchronized void writeObject(Object obj) throws Exception {
			getOut().reset();
			getOut().writeObject(obj);
			getOut().flush();
			getOut().reset();
		}
		
		@Override
		public TCPResponse sendRequest(TCPRequest request) throws Exception {
			return sendRequest(request, defaultResponseTimeout);
		}
		
		@Override
		public TCPResponse sendRequest(TCPRequest request, long resultTimeout) throws Exception {
			//为将要发送的命令指定一个唯一的命令ID，以匹配服务端送回的命令结果
			log.debug(buildNetLog(MessageFormat.format("TCPNetClient ready to send request[{0}], request id is {1}", request, request.getId())));
			
			//设置接受结果的缓存
			RequestResult result = new RequestResult();
			synchronized(resultCache) {
				resultCache.put(request.getId(), result);
			}
			
			//发送命令对象到服务端
			try {
				log.debug(request.toString());
				writeObject(request);
				
				log.debug(buildNetLog((MessageFormat.format("TCPNetClient has sent: {0}", request))));
			} catch (Exception e) {
				log.debug(buildNetLog("TCPNetClient write object error"), e);

				synchronized(resultCache) {
					resultCache.remove(request.getId());
				}

				throw e;
			}
			
			//等待结果对象返回
			log.debug(buildNetLog("TCPNetClient begin to wait for command result"));
			
			boolean isTimeout = false;
			TCPResponse responseObj = null;
			
			synchronized(resultCache) {
				long t1 = System.currentTimeMillis();
				while(result.response == null) {
					try {
						resultCache.wait(100);
					} catch (Exception e){
						log.debug("exception",e);
					}
					
					long t2 = System.currentTimeMillis();
					if((result.response == null) && (t2 - t1 >= resultTimeout)) {
						isTimeout = true;
						break;
					}
				}
				
				if(!isTimeout)
					responseObj = result.response;
				
				resultCache.remove(request.getId());
			}
			
			//没有等到结果，表示结果等待超时
			if(isTimeout) {
				log.debug(buildNetLog("wait for response timeout"));
				throw new Exception("Waiting for response timeout,  request.id:"+request.getId());
			}

			return responseObj;
		}

		@Override
		public void run() {
			addProcessor(this);
			for(TCPNetServerListener tl : listeners) {
				tl.onConnected(this);
			}
			try {
				try {
					while (!stop) {
						Object obj = null;
						try {
							obj = getIn().readObject();
						}
						catch(IOException e){
							log.debug(buildNetLog("exception"),e);
						}
						catch(Exception e) {
							log.debug(buildNetLog("net server receive command error"), e);							
							continue;
						}
						
						if(obj == null) {
							break;
						}
						else if(obj instanceof TCPRequest) {
							final TCPRequest request = (TCPRequest)obj;
							log.debug("TCPNetServer receive Obj Is TCPRequest, Request Data is :"+ request.getData().toString());
							final TCPResponse response = new TCPResponse(request.getId(), null);
							if(request.getData() instanceof TCPHeartbeat) {
								writeObject(response);
							}
							else {
								new Thread() {
									@Override
									public void run() {
										log.debug(buildNetLog(MessageFormat.format("server process TCPRequest start time: {0}", System.currentTimeMillis())));
										for(TCPNetServerListener tl : listeners) {
											tl.onRequest(TCPNetSessionProcessor.this, request, response);
										}
										log.debug(buildNetLog(MessageFormat.format("server process TCPRequest end, ready to send result start time: {0}", System.currentTimeMillis())));
										try {
											writeObject(response);
										} catch (Exception e) {
											log.debug(buildNetLog("tcp net server send response to server failed"), e);
										}
										log.debug(buildNetLog(MessageFormat.format("server process TCPRequest end, send result end time: {0}", System.currentTimeMillis())));
									}
								}.start();
							}
						}
						else if(obj instanceof TCPResponse) {
							if(((TCPResponse)obj).getData() != null){							
								log.debug("TCPNetServer receive Obj Is TCPResponse, Response Data is :"+ ((TCPResponse)obj).getData().toString());
							}else{
								log.debug("TCPNetServer receive Obj Is TCPResponse, Response Data is TCPHeartBeat.");
							}
							setRequestResult((TCPResponse)obj);
						}
						else if(obj instanceof TCPMessage) {
							final TCPMessage message = (TCPMessage)obj;
							log.debug("TCPNetServer receive Obj Is TCPMessage, Message Data is :"+ message.getData().toString());
							new Thread() {
								@Override
								public void run() {
									for(TCPNetServerListener tl : listeners) {
										tl.onMessage(TCPNetSessionProcessor.this, message);
									}
								}
							}.start();
						}
					}
				} catch (IOException e){
					log.debug(buildNetLog("net server exception"), e);
				} catch (Exception e) {
					log.debug(buildNetLog("net server exception"), e);
				}
				log.info(buildNetLog("Net client disconnected!"));
				
				//关闭socket资源
				try {
					socket.close();
					stop = true;
				}
				catch(Exception e) {
					log.debug(buildNetLog("exception"),e);
				}
			}
			finally {
				connected = false;
				
				for(TCPNetServerListener tl : listeners) {
					tl.onClosed(this);
				}
				
				removeProcessor(this);
			}
		}

		public void start() {
			if(thread == null) {
				stop = false;
				thread = new Thread(this);
				thread.start();
				
				new HeartbeatThread().start();
			}
		}
		
		public void stop() {
			//关闭socket资源
			try {
				stop = true;
				socket.close();
			}
			catch(Exception e) {
				log.debug(buildNetLog("exception"),e);
			}
		}
		
		public void setRequestResult(TCPResponse response) {
			synchronized(resultCache) {
				RequestResult result = resultCache.get(response.getId());
				if(result == null) {
					log.debug(buildNetLog(MessageFormat.format("command result already destroyed, can not set command result: {0}", response)));
				}
				else {
					result.response = response;
					
					try {
						resultCache.notifyAll();
					} catch (Exception e){
						log.debug(buildNetLog("exception"),e);
					}
				}
				log.debug(buildNetLog("TCPNetClient done set command result: " + response));
			}
		}

		@Override
		public boolean isConnected() {
			return connected;
		}

	}
	
	public TCPNetServer() {}
	
	/**
	 *       得到连接到服务端的所有TCPNetSessionProcessor
	 *  lqh add
	 */
	public Set<TCPNetSessionProcessor> getProcessors(){
		return this.processors;
	}
	
	public void addListener(TCPNetServerListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(TCPNetServerListener listener) {
		listeners.remove(listener);
	}
	
	public void start(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		
		stop = false;
		Thread t = new Thread(null, this, "tcp net server listener thread");
		t.start();
	}
	
	public void stop() throws IOException {
		stop = true;
		
		if(serverSocket != null) {
			try {
				serverSocket.close();
			}
			catch(Exception e){
				log.debug("exception",e);
			}
		}
		
		stopAllProcessor();
	}
	
	private synchronized void stopAllProcessor() {
		for(TCPNetSessionProcessor tp : processors) {
			tp.stop();
		}
	}
	

	
	private synchronized void addProcessor(TCPNetSessionProcessor processor) {
		processors.add(processor);
	}
	
	private synchronized void removeProcessor(TCPNetSessionProcessor processor) {
		processors.remove(processor);
	}
	
	@Override
	public void run() {
		alive = true;
		while(!stop) {
			try {
				Socket socket = serverSocket.accept();
				TCPNetSessionProcessor processor = new TCPNetSessionProcessor(socket);
				processor.start();
			} catch (IOException e) {
				if(!stop)
					log.debug("NwServer run error", e);
			}
		}
		alive = false;
	}
	
	public boolean isAlive() {
		return alive;
	}
}
