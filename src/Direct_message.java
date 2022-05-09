import java.io.Serializable;

public class Direct_message implements Serializable {
	private static final long serialVersionUID = 1234L;
	private String from;
	private String to;
	private String msg;
	 
	public Direct_message(String from, String to, String msg) {
		this.from = from;
		this.to = to;
		this.msg = msg;
	}

	public String get_to() {
		return this.to;
	}

	public String get_from() {
		return this.from;
	}

	public String get_msg() {
		return this.msg;
	}
}