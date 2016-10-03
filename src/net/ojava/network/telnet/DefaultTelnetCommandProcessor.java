package net.ojava.network.telnet;

import java.util.Hashtable;

import net.ojava.network.telnet.log.NetLogger;

public class DefaultTelnetCommandProcessor implements ITelnetCommandProcessor {
	enum COMMAND_TYPE{
		VERSION,
		LISTCLIENT,
		GETLOGLEVEL,
		SETLOGLEVEL,
		SETFILELOGLEVEL,
		SETTELNETLOGLEVEL
	};
	private static final String[][] commands = {
		{"version", "version", "view telnet server version"},
		{"lsclient", "lsclient", "view all connecting clients, no paramater"},
		{"getloglevel", "getloglevel", "view file log level and telnet log level"},
		{"setloglevel", "setloglevel <loglevel>", "set file and telnet log level: error, warn, info, debug"},
		{"setfileloglevel", "setfileloglevel <loglevel>", "set file log level: error, warn, info, debug"},
		{"settelnetloglevel", "settelnetloglevel <loglevel>", "set telnet log level: error, warn, info, debug"}
	};
	private static final Hashtable<String, Integer> commandIndexes = new Hashtable<String, Integer>();
	
	private TelnetServer server;
	
	static {
		for(int i=0; i<commands.length; i++) {
			commandIndexes.put(commands[i][0], i);
		}
	}
	
	public DefaultTelnetCommandProcessor(TelnetServer server) {
		this.server = server;
	}

	@Override
	public String getDescription() {
		return "telnet basic commands module";
	}

	@Override
	public String help() {
		StringBuffer sb = new StringBuffer();
		sb.append("Telnet Basic Commands:\r\n\r\n");
		sb.append("bye : close telnet connection\r\n");
		for(int i=0; i<commands.length; i++) {
			sb.append(commands[i][1]);
			sb.append(" : ");
			sb.append(commands[i][2]);
			sb.append("\r\n");
		}

		return sb.toString();
	}

	@Override
	public String process(String[] args) {
		Integer index = commandIndexes.get(args[0]);
		if(index == null)
			return null;
		
		switch(COMMAND_TYPE.values()[index]) {
		case LISTCLIENT:
			return listClient();
		case VERSION:
			return version();
		case GETLOGLEVEL:
			return printLogLevel();
		case SETLOGLEVEL:
			if(args.length < 2) {
				return "please give a parameter for log level";
			}else{
				try {
					return setLogLevel(args[1]);
				} catch (Exception e) {
					return e.getMessage();
				}
			}
		case SETFILELOGLEVEL:
			if(args.length < 2) {
				return "please give a parameter for log level";
			}else{
				try {
					return setFileLogLevel(args[1]);
				} catch (Exception e) {
					return e.getMessage();
				}
			}
		case SETTELNETLOGLEVEL:
			if(args.length < 2) {
				return "please give a parameter for log level";
			}else{
				try {
					return setTelnetLogLevel(args[1]);
				} catch (Exception e) {
					return e.getMessage();
				}
			}
		}
		
		return null;
	}
	
	private String listClient() {
		StringBuffer sb = new StringBuffer();
		int ip=0;
		for(TelnetServerSession session : server.getSessions()) {
			ip++;
			
			sb.append("clients list:\r\n");
			sb.append("client");
			sb.append(ip);
			sb.append(": ");
			sb.append(session.getRemoteAddress());
			sb.append("\r\n");
		}
		
		return sb.toString();
	}
	
	private String version() {
		StringBuffer sb = new StringBuffer();
		sb.append("Telnet Server Version: ");
		sb.append(TelnetServer.VERSION);
		
		return sb.toString();
	}
	
	private String printLogLevel() {
		return NetLogger.printLevel();
	}
	
	private String setLogLevel(String level) throws Exception {
		NetLogger.setLevel(level);
			
		return "set file and telnet log level successfully - " + level;
	}

	private String setFileLogLevel(String level) throws Exception {
		NetLogger.setFileLogLevel(level);
			
		return "set file log level successfully - " + level;
	}

	private String setTelnetLogLevel(String level) throws Exception {
		NetLogger.setTelnetLogLevel(level);
			
		return "set telnet log level successfully - " + level;
	}
}
