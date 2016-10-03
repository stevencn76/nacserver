package nacserver.cache;


public class PkCacheItem {
	public static final int TURN_INVITE = 0;
	public static final int TURN_ACCEPT = 1;
	
	public static final int STATUS_WAITACCEPT = 1;
	public static final int STATUS_GAMING = 2;
	public static final int STATUS_END = 3;
	
	private int status;
	private String playerName1;
	private String playerName2;
	private int gameData;
	private int curTurn;
	
	public static int getRandomTurn() {
		return ((int)(Math.random() * 2)) == 0 ? TURN_INVITE : TURN_ACCEPT;
	}
	
	public String getPlayerName1() {
		return playerName1;
	}
	public void setPlayerName1(String playerName1) {
		this.playerName1 = playerName1;
	}
	public String getPlayerName2() {
		return playerName2;
	}
	public void setPlayerName2(String playerName2) {
		this.playerName2 = playerName2;
	}
	public int getGameData() {
		return gameData;
	}
	public void setGameData(int gameData) {
		this.gameData = gameData;
	}
	public int getCurTurn() {
		return curTurn;
	}
	public void setCurTurn(int curTurn) {
		this.curTurn = curTurn;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
}
