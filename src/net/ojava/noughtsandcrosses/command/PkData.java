package net.ojava.noughtsandcrosses.command;

import java.io.Serializable;
import java.util.HashMap;

public class PkData implements Serializable {
	private static final long serialVersionUID = -4564112291506336695L;

	public String name1;
	public String name2;
	public HashMap<String, Integer> turnIds = new HashMap<String, Integer>();
	public HashMap<String, Integer> signIds = new HashMap<String, Integer>();
    public int gameData;
    public int nextTurnId;
    public boolean gameOver;
}
