import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {
	private static final long serialVersionUID = 1234L;

	public static final	int USERNAME = 1;
	public static final int GROUPMSG = 2;
	public static final int DIRECT = 3;
	public static final int ACCEPT = 4;
	public static final int REJECT = 5;
	public static final int CALL = 6;
	public static final int ENDCALLS = 7;
	public static final int VOICEMESSAGE = 8;
	public static final int DISCONNECT = 0;

	private int header;
	private String data;	
	private Direct_message dm;
	// if Call then list of users in CALL
	private ArrayList<Users> inCall;
	private Voice_message vmessage;


	public Message(int header, String data) {
		this.header = header;
		this.data = data;
	}
	public void setVoiceMessage(Voice_message vomessage) {
		vmessage = vomessage;
	}
	public Voice_message get_VoiceMessage() {
		return vmessage;
	}

	public int get_header() {
		return header;
	}

	public String get_data() {
		return data;
	}

	public Direct_message get_dm() {
		return dm;
	}

	public void set_dm(Direct_message dm) {
		this.dm = dm;
	}
	public void set_inCall(ArrayList<Users> inCall){
		this.inCall = inCall;
	}
	public ArrayList<Users> get_inCall() {
		return inCall;
	}

	@Override
	public String toString() {
		return String.format("MSG = (header: %s || data: %s)", header, data);
	}

}
