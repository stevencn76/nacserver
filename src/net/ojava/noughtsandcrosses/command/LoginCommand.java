package net.ojava.noughtsandcrosses.command;

import java.io.Serializable;

public class LoginCommand implements Serializable {
	private static final long serialVersionUID = -4564112291506336695L;

	public String name;
    public String password;
    public String result;
}
