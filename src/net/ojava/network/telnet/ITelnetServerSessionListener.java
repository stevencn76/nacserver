package net.ojava.network.telnet;

public interface ITelnetServerSessionListener {
	public void sessionStoped(TelnetServerSession session);
	public void commandArrived(TelnetServerSession session, String command);
}
