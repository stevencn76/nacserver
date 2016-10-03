package nacserver.dto;

import javax.xml.bind.annotation.XmlRootElement;

import nacserver.entity.Player;

@XmlRootElement
public class PlayerTO {
	private int id;
	private String name;
	private String ip;
	private String rival;
	private String status;
	private String createTimeStr;
	private String lastTimeStr;
	
	public static PlayerTO from(Player p) {
		PlayerTO tp = new PlayerTO();
		
		tp.setId(p.getId());
		tp.setName(p.getName());
		tp.setIp(p.getIp());
		tp.setCreateTimeStr(p.getCreateTimeStr());
		tp.setLastTimeStr(p.getLastTimeStr());
		
		return tp;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getRival() {
		return rival;
	}

	public void setRival(String rival) {
		this.rival = rival;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCreateTimeStr() {
		return createTimeStr;
	}

	public void setCreateTimeStr(String createTimeStr) {
		this.createTimeStr = createTimeStr;
	}

	public String getLastTimeStr() {
		return lastTimeStr;
	}

	public void setLastTimeStr(String lastTimeStr) {
		this.lastTimeStr = lastTimeStr;
	}
}
