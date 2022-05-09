import java.io.Serializable;
import java.util.ArrayList;

public class Client_obj implements Serializable {

	private final long serialVersionUID = 1234L;
	private int header;
	private ArrayList<Direct_message> dm;
	private ArrayList<Users> users;
	private ArrayList<String> group_msgs;
	private ArrayList<Users> Userscall;
	private Voice_message voicemes;
	

	public Client_obj() {
		this.dm = new ArrayList<Direct_message>();
		this.users = new ArrayList<Users>();
		this.group_msgs = new ArrayList<String>();
		header = 0;
	}
	public synchronized void update_voicemes(Voice_message mes) {
		voicemes = mes;
	}
	public synchronized Voice_message get_voicemes() {
		return voicemes;
	}
	public synchronized void update_direct(Direct_message msg) {
		dm.add(msg);
	}


	public synchronized void update_group(String msg) {
		group_msgs.add(msg);
	}
	public synchronized void set_Usercall(ArrayList<Users> call) {
		Userscall = call;
	}
	public synchronized int get_header() {
		return header;
	}

	public synchronized ArrayList<Users> get_inCall() {
		return Userscall;
	}

	public synchronized void update_users(String username, boolean avaiable) {
		Users temp = new Users(username, avaiable);
		users.add(temp);
	}

	public synchronized void remove_user(String username) {
		for(int i = 0; i < users.size(); i++) {
			if (users.get(i).get_Username().equals(username)) {
				users.remove(i);
			}

		}
	}
	//check if all users are availabel and if they are then sett all of them to unavailable 
	public synchronized boolean allUsersAvailable(ArrayList<Users> toCall) {
		for (Users U : toCall) {
			for (Users check : users) {
				if (U.get_Username().equals(check.get_Username())) {
					if (!check.get_Available()) {
						return false;
					}
				}
			}
			
		}
		for (Users U : toCall) {
			for (Users check : users) {
				if (U.get_Username().equals(check.get_Username())) {
					check.set_available(false);
				}
			}
		}
		return true;
	}
	public synchronized void setAllAvailable() {
		for (Users U : users) {
			U.set_available(true);
		}
	}

	public synchronized ArrayList<String> get_groupmsg() {
		return this.group_msgs;
	}

	public synchronized ArrayList<Direct_message> get_dm() {
		return this.dm;
	}

	public synchronized ArrayList<Users> get_users() {
		return this.users;
	}

	public synchronized void clear_direct() {
		this.dm.clear();
	}
	public synchronized void set_header(int i){
		header = i;
	}

	public synchronized String toString() {
		return "GRP: " + this.group_msgs.toString() + "\n" + "DIR: " + this.dm.toString() + "\n" + "USR: " + this.users.toString()+ "\n";
	}
}
