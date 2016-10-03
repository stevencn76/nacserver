package net.ojava.network.tcp;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.apache.log4j.Logger;

/*
 * TCPNetClient可以用于与TCPNetServer进行连接和传送数据
 * 该类的使用步骤为：
 * 1. 创建该类的实例
 * 2. 注册监听器(TCPNetClientListener实例)
 * 3. 使用connect方法连接服务端
 * 4. 使用sendRequest发送请求
 * 5. 使用postMessage发送消息
 * 6. 使用disconnect断开连接
 * 
 * 作者：陈宝峰
 * 创建日期：2011-07-20
 * 最后修改日期：2011-11-25
 * 修改日志：
 * 2011-11-25 第一次修改 增加发送TCPRequest时等待结果的超时日志输出
 */
public class TCPNetClient implements Runnable {
	private static Logger log = Logger.getLogger(TCPNetClient.class);
	private static final int defaultResponseTimeout = 60000;
	private static final int defaultHeartbeatSpaceTime = 10000;
	private static final int defaultHeartbeatResponseTimeout = 10000;
	
	private final Object writeObjectLock = new Object();
	
	private Set<TCPNetClientListener> listeners = new HashSet<TCPNetClientListener>();
	
	private String remoteAddress;
	private int remotePort;
	private String localAddress;
	private int localPort;

	private Socket socket;
	
	private boolean connected = Boolean.valueOf(false);
	
	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	private ObjectInputStream objectInputStream;
	private ObjectOutputStream objectOutputStream;
	
	private boolean stop = false;
	
	private class RequestResult{
		public TCPResponse response;
		
		public RequestResult() {
			response = null;
		}
	}
	private Hashtable<String, RequestResult> resultCache = new Hashtable<String, RequestResult>();
	
	private class HeartbeatThread extends Thread {
		@Override
		public void run() {
			while(!stop) {
				try {
					Thread.sleep(defaultHeartbeatSpaceTime);
				}
				catch(Exception e){}
				
				try {
					sendRequest(new TCPRequest(new TCPHeartbeat()), defaultHeartbeatResponseTimeout);
				}
				catch(Exception e) {
					if(!stop) {
						log.debug(buildNetLog("Heartbeat request failed"), e);
						
						new Thread("notify_processor_thread") {
							@Override
							public void run() {
								for(TCPNetClientListener tl : listeners) {
									tl.onInterrupt(remoteAddress,String.valueOf(remotePort));
								}
							}
						}.start();
						
						close();
						break;
					}
				}
			} 
		}
	}
	
	public TCPNetClient() {
	}
	
	public String getRemoteAddress() {
		return remoteAddress;
	}

	public int getRemotePort() {
		return remotePort;
	}
	
	public void setRemotePort(int port){
		this.remotePort = port;
	}

	public String getLocalAddress() {
		return localAddress;
	}

	public int getLocalPort() {
		return localPort;
	}
	
	public void addListener(TCPNetClientListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(TCPNetClientListener listener) {
		listeners.remove(listener);
	}
	
	private String buildNetLog(String content) {
		return MessageFormat.format("[remote ip: {0}, remote port: {1}] {2}", getRemoteAddress(), getRemotePort(), content);
	}

	public void connect() throws Exception {
		close();
		
		log.debug(buildNetLog("ready to connect"));
		socket = new Socket(remoteAddress, remotePort);
		
		connected = true;

		localAddress = socket.getLocalAddress().getHostAddress();
		localPort = socket.getLocalPort();
		
		objectInputStream = null;
		objectOutputStream = null;
		stop = false;
		//启动接受服务器发送notify对象的线程
		Thread t = new Thread(null, this, "tcpnetclient_receiver_listener");
		t.start();
		
		//启动心跳包线程
		new HeartbeatThread().start();
	}

	public void connect(String remoteAddress, int remotePort) throws Exception {
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
		
		connect();
	}

	public void close(){
		log.debug(buildNetLog("ready to close socket"));
		try{
			stop = true;
			if (socket != null) {
				socket.close();
				socket = null;
			}
			log.debug("TCPNetClient closed");
		}catch(Exception e){
			log.debug("TCPNetClient close failed", e);
		}
	}	
	
	public void reset() {
		stop = false;
	}
	
	public boolean isConnected() {
		return connected;
	}
	public void disConnect() {
		connected=false;
	}
	
	public void postMessage(TCPMessage message) throws Exception {
		writeObject(message);
	}
	
	private void writeObject(Object obj) throws Exception {
		synchronized(writeObjectLock) {
			log.debug(MessageFormat.format("start write object[{0}] to ip:{1}, port:{2}", obj, this.getRemoteAddress(), this.getRemotePort()));
			getOut().reset();
			getOut().writeObject(obj);
			getOut().flush();
			getOut().reset();
			log.debug(MessageFormat.format("complete write object[{0}] to ip:{1}, port:{2}", obj, this.getRemoteAddress(), this.getRemotePort()));
		}
	}
	
	public TCPResponse sendRequest(TCPRequest request) throws Exception {
		return sendRequest(request, defaultResponseTimeout);
	}
	
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
			
			log.debug(buildNetLog(MessageFormat.format("TCPNetClient has sent: {0}", request)));
		} catch (Exception e) {
			log.debug(buildNetLog("TCPNetClient write object error. request : + " + request +"request id is : " + request.getId()), e);

			synchronized(resultCache) {
				resultCache.remove(request.getId());
			}

			throw e;
		}
		
		//等待结果对象返回
		log.debug(buildNetLog(MessageFormat.format("TCPNetClient begin to wait for command result, timeout is {0}", resultTimeout)));
		
		boolean isTimeout = false;
		TCPResponse responseObj = null;
		
		synchronized(resultCache) {
			long t1 = System.currentTimeMillis();
			while(result.response == null) {
				try {
					resultCache.wait(100);
				} catch (Exception e){
					log.debug(buildNetLog("exception"),e);
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
			throw new Exception("等待服务器返回结果超时 request.id : " + request.getId());
		}

		return responseObj;
	}
	
	@Override
	public void run() {
		while(!stop) {
				log.debug(buildNetLog("TCPNetClient wait for object from server"));
				Object obj = null;
				try {
					obj = getIn().readObject();
					log.debug(buildNetLog(MessageFormat.format("TCPNetClient received: {0}", obj==null? "null" : obj.toString())));

					if(obj == null) {
						if(!stop) {
							new Thread("notify_processor_thread") {
								@Override
								public void run() {
									for(TCPNetClientListener tl : listeners) {
										log.debug("TCPNetClient onClosed Method Start ...");
										tl.onClosed();
									}
								}
							}.start();
						}
						break;
					}
					else if(obj instanceof TCPRequest) {
						final TCPRequest request = (TCPRequest)obj;
						final TCPResponse response = new TCPResponse(request.getId(), null);
						log.debug("TCPNetClient receive Obj Is TCPRequest, Request Data is :"+ request.getData().toString());
						if(request.getData() instanceof TCPHeartbeat) {
							writeObject(response);
						}
						else {
							new Thread() {
								@Override
								public void run() {
									for(TCPNetClientListener tl : listeners) {
										tl.onRequest(request, response);
									}
									try {
										writeObject(response);
									} catch (Exception e) {
										log.debug(buildNetLog("tcp net client send response to server failed"), e);
									}
								}
							}.start();
						}
					}
					else if(obj instanceof TCPResponse) {
						if(((TCPResponse)obj).getData() != null){							
							log.debug("TCPNetClient receive Obj Is TCPResponse, Response Data is :"+ ((TCPResponse)obj).getData().toString());
						}else{
							log.debug("TCPNetClient receive Obj Is TCPResponse, Response Data is TCPHeartBeat.");
						}
						setRequestResult((TCPResponse)obj);
					}
					else if(obj instanceof TCPMessage) {
						final TCPMessage message = (TCPMessage)obj;
						log.debug("TCPNetClient receive Obj Is TCPMessage, Message Data is :"+ message.getData().toString());
						new Thread() {
							@Override
							public void run() {
								for(TCPNetClientListener tl : listeners) {
									tl.onMessage(message);
								}
							}
						}.start();
					}
				}catch(ClassNotFoundException e) {
					log.debug(buildNetLog("TCPNetClient read object error"), e);
					final String msg = e.getMessage();
					new Thread("notify_processor_thread") {
						@Override
						public void run() {
							for(TCPNetClientListener tl : listeners) {
								tl.onUnknowData(msg);
							}
						}
					}.start();
				}
				catch(InvalidClassException e) {
					log.debug(buildNetLog("TCPNetClient read object error"), e);
					final String msg = e.getMessage();
					new Thread("notify_processor_thread") {
						@Override
						public void run() {
							for(TCPNetClientListener tl : listeners) {
								tl.onUnknowData(msg);
							}
						}
					}.start();
				}
				catch(StreamCorruptedException e) {
					log.debug(buildNetLog("TCPNetClient read object error"), e);
					final String msg = e.getMessage();
					new Thread("notify_processor_thread") {
						@Override
						public void run() {
							for(TCPNetClientListener tl : listeners) {
								tl.onUnknowData(msg);
							}
						}
					}.start();
				}
				catch(Exception e){
					log.debug(buildNetLog("TCPNetClient read object error"), e);
					new Thread("notify_processor_thread") {
						@Override
						public void run() {
							for(TCPNetClientListener tl : listeners) {
								tl.onInterrupt(remoteAddress,String.valueOf(remotePort));
							}
						}
					}.start();
					
					break;
				}
		}

		log.debug(buildNetLog(MessageFormat.format("TCPNetClient receiver loop end[stop:{0}]", stop)));
		
		connected = false;
		stop = true;
		close();
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
	

	private final Object getInLock = new Object();
	private ObjectInputStream getIn() throws IOException {
		synchronized(getInLock) {
			if(objectInputStream == null) {
				objectInputStream = new ObjectInputStream(socket.getInputStream());
				log.debug(this + "   ; remoteAddress " + remoteAddress + " ; create ObjectInputStream : " + objectInputStream);
			}

		}
			
			return objectInputStream;
		
	}
	

	private final Object getOutLock = new Object();
	private ObjectOutputStream getOut() throws IOException {
		synchronized(getOutLock) {
			if(objectOutputStream == null) {
				objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
				log.debug(this + "   ; remoteAddress " + remoteAddress + " ; create ObjectOutputStream : " + objectOutputStream);
			}
		}
			
			return objectOutputStream;
		
	}
}
