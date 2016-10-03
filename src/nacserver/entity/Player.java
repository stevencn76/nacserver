package nacserver.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import nacserver.util.TimeUtil;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.GenericGenerator;


@Entity
@Table(name = "t_player")
@XmlRootElement
@JsonIgnoreProperties({ "hibernateLazyInitializer" })
public class Player  extends BaseEntity {
	private static final long serialVersionUID = 2086040663326998265L;
	
	private Integer id;
	private String name;
	private String password;
	private String ip;
	private Timestamp createTime;
	private String createTimeStr;
	private Timestamp lastTime;
	private String lastTimeStr;
	
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	@Column(name = "id")
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	@Column(name = "name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Column(name = "password")
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	@Column(name = "ip")
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	
	@Column(name = "create_time")
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
		if(createTime != null)
			setCreateTimeStr(TimeUtil.getSQLTimestamp(createTime));
		else
			setCreateTimeStr("");
	}
	
	@Transient
	public String getCreateTimeStr() {
		return createTimeStr;
	}
	public void setCreateTimeStr(String createTimeStr) {
		this.createTimeStr = createTimeStr;
	}
	
	@Column(name = "last_time")
	public Timestamp getLastTime() {
		return lastTime;
	}
	public void setLastTime(Timestamp lastTime) {
		this.lastTime = lastTime;
		if(createTime != null)
			setLastTimeStr(TimeUtil.getSQLTimestamp(lastTime));
		else
			setLastTimeStr("");
	}
	
	@Transient
	public String getLastTimeStr() {
		return lastTimeStr;
	}
	public void setLastTimeStr(String lastTimeStr) {
		this.lastTimeStr = lastTimeStr;
	}
}
