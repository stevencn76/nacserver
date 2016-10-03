package net.ojava.noughtsandcrosses.command;

import java.io.Serializable;

public class InviteCommand implements Serializable {
	private static final long serialVersionUID = -4564112291506336695L;

	public String inviterName;
	public String accepterName;
    public String result;
    public PkData pkData;
}
