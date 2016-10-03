package net.ojava.network.telnet.log;

import java.util.Hashtable;

import net.ojava.network.telnet.TelnetServer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

@SuppressWarnings("rawtypes")
public class NetLogger {
	private Logger log4jLogger;
	private static TelnetServer telnetServer;
	private static Level telnetLogLevel = Level.ERROR;
	public static final String ERROR = "ERROR";
	public static final String WARN = "WARN";
	public static final String INFO = "INFO";
	public static final String DEBUG = "DEBUG";
	
	private static final Hashtable<Class, NetLogger> loggerMap = new Hashtable<Class, NetLogger>();
	
	private NetLogger(){}
	
	public static NetLogger getLogger(Class c) {
		NetLogger logger = loggerMap.get(c);
		if(logger == null) {
			logger = new NetLogger();
			logger.log4jLogger = Logger.getLogger(c);
			loggerMap.put(c, logger);
		}
		
		return logger;
	}
	
	public static void setTelnetServer(TelnetServer server) {
		telnetServer = server;
	}
	
	public static TelnetServer getTelnetServer() {
		return telnetServer;
	}
	
	private void sendTelnetLog(Level level, Object message) {
		if(telnetServer != null && level.isGreaterOrEqual(telnetLogLevel)) {
			telnetServer.sendResponse(message.toString());
		}
	}
	
	private void sendTelnetLog(Level level, Object message, Throwable t) {
		if(telnetServer != null && level.isGreaterOrEqual(telnetLogLevel)) {
			telnetServer.sendResponse(message.toString());
			telnetServer.sendResponse(t);
		}
	}
	
	public static void setTelnetLogLevel(Level level) {
		NetLogger.telnetLogLevel = level;
	}
	
	public void debug(Object message){
		log4jLogger.debug(message);
		sendTelnetLog(Level.DEBUG, message);
	}
	
	public void debug(Object message, Throwable t) {
		log4jLogger.debug(message, t);
		sendTelnetLog(Level.DEBUG, message, t);
	}
	
	public void info(Object message){
		log4jLogger.info(message);
		sendTelnetLog(Level.INFO, message);
	}
	
	public void info(Object message, Throwable t) {
		log4jLogger.info(message, t);
		sendTelnetLog(Level.INFO, message, t);
	}
	
	public void warn(Object message){
		log4jLogger.warn(message);
		sendTelnetLog(Level.WARN, message);
	}
	
	public void warn(Object message, Throwable t) {
		log4jLogger.warn(message, t);
		sendTelnetLog(Level.WARN, message, t);
	}
	
	public void error(Object message){
		log4jLogger.error(message);
		sendTelnetLog(Level.ERROR, message);
	}
	
	public void error(Object message, Throwable t) {
		log4jLogger.error(message, t);
		sendTelnetLog(Level.ERROR, message, t);
	}
	
	public static String printLevel() {
		StringBuilder str = new StringBuilder();
		str.append("file log level: ");
		str.append(Logger.getRootLogger().getLevel().toString());
		str.append("\r\n");
		str.append("telnet log level: ");
		str.append(telnetLogLevel.toString());
		
		return str.toString();
	}
	
	private static Level parseLevel(String level) {
		level = level.toUpperCase();
		Level logLevel = null;
		if(ERROR.equals(level)){
			logLevel = Level.ERROR;
		}else if(WARN.equals(level)){
			logLevel = Level.WARN;
		}else if(INFO.equals(level)){
			logLevel = Level.INFO;
		}else if(DEBUG.equals(level)){
			logLevel = Level.DEBUG;
		}
		
		return logLevel;
	}
	
	public static String setLevel(String level) throws Exception{
		Level logLevel = parseLevel(level);
		if(logLevel != null){
			Logger.getRootLogger().setLevel(logLevel);
			telnetLogLevel = logLevel;
			return level;
		}else
			throw new Exception("the option is not correct. level option: error,warn,info,debug.");
	}
	
	public static String setFileLogLevel(String level) throws Exception {
		Level logLevel = parseLevel(level);
		if(logLevel != null){
			Logger.getRootLogger().setLevel(logLevel);
			return level;
		}else
			throw new Exception("the option is not correct. level option: error,warn,info,debug.");
	}
	
	public static String setTelnetLogLevel(String level) throws Exception {
		Level logLevel = parseLevel(level);
		if(logLevel != null){
			telnetLogLevel = logLevel;
			return level;
		}else
			throw new Exception("the option is not correct. level option: error,warn,info,debug.");
	}
}
