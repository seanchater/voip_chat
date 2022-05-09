import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import java.awt.EventQueue;

import javax.swing.*;

import java.awt.event.*;

public class Gui extends Thread {

	public static JFrame frame;
	private JTextField textField;
	private JTextField msg;
	private static JButton btnNewButton;
	private static JTextArea chatroom;
	private static DefaultListModel<String> listModel;
	private static ObjectInputStream in;
	private static ObjectOutputStream out;
	private static Client_conn c;
	private static String clientusername;
	public static String my_hostname;
	public static String server_name;
	private static Socket socket;
	private static byte[] voicefile;
	public static boolean endvn = false;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		// connect to hamachi

		if (args.length != 2) {
			System.err.println(
				"Usage: java EchoClient <my hostname> <server name");
			System.exit(1);
		}

		my_hostname = args[0];
		String server_name = args[1];

		// init socket connection and in/out streams

		init(server_name, 8000);
		
		// create a new thread to listen to the server for any incoming group / direct msgs
		c = new Client_conn(socket, in, my_hostname, 0, null);
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Gui window = new Gui();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Gui() {
		
		verify_user(my_hostname);
		initializenew();
		c.start();
	}

	/**
	 * Initialises the socket to the server and the input/output streams of the tcp socket
	 * @param host_name the host of the server
	 * @param port port number of the server
	 */
	public static void init(String host_name, int port) {
		try {
			socket = new Socket(host_name, port);
			// System.out.println("Client socket opened successfully on port " + socket.getPort());
			in = new ObjectInputStream(socket.getInputStream());
			out = new ObjectOutputStream(socket.getOutputStream());
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.err.println("Don't know about host " + host_name);
	 	} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Couldn't get I/O for the connection to " + host_name);
		}
	}

	/**
	 * Verifies that the username is not a duplicate
	 * @param username whatever the user entered
	 * @return true if username is valid, false otherwise
	 */
	public static void verify_user(String username) {
		try {
				// and create a user object to write to server
				Message username_msg = new Message(Message.USERNAME, username);
				// System.out.printf("[CLIENT SENT] %s\n", username_msg.toString());
				out.writeObject(username_msg);

				// get response from server
				Message server_resp = (Message) in.readObject();
				// System.out.printf("[CLIENT RECV] %s\n", server_resp.toString());
				// if resp says unique username then allow to chat
				if (server_resp.get_header() == Message.ACCEPT) {
					 System.out.println("username was valid - allowed to chat");
					clientusername = username;
				} else if (server_resp.get_header() == Message.REJECT) {
					System.out.println("username not valid - try again");
				} else {
					System.err.println("server error when receiving username accept");
				}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("I/O error for connection to " + socket.getInetAddress());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.err.printf("Message class (%s) not found when reading resp from socket\n", e.getClass());
		}
	}




	/**
	 * Sends a message object to the server
	 * @param user_input whatever the user typed in if it is a group msg or a disconnect
	 * @param dm dm to be sent if the message is a whisper msg
	 */
	public static void send_message(String user_input, Direct_message dm,ArrayList<Users> inCall,Voice_message vmessage) {
		Message msg;
		try {
			// get msg object from gui method
			if (user_input.equals("exit")) {
				msg = new Message(Message.DISCONNECT, null);
				out.writeObject(msg);
				disconnect();
			} else if (user_input.equals("direct_msg")) {
				msg = new Message(Message.DIRECT, null);
				msg.set_dm(dm);
				out.writeObject(msg);
			} else if (user_input.equals("")) {
				msg = new Message(Message.CALL, null);
				msg.set_inCall(inCall);
				// System.out.printf("[SEND] %s\n", user_input);
				out.writeObject(msg);
			}  else if (user_input.equals("End")){
				msg = new Message(Message.ENDCALLS, null);
				out.writeObject(msg);
			}
			else if (user_input.equals("VN")) {
				msg = new Message(Message.VOICEMESSAGE, null);
				msg.setVoiceMessage(vmessage);
				out.writeObject(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error when reading user input");
		}
	}

	public static void disconnect() {
		try {
			if (in != null) in.close();
		} catch (Exception e) {}
		try {
			if (out != null) out.close();
		} catch (Exception e) {}
		try {
			if (socket != null) socket.close();
		} catch (Exception e) {}

		// close the gui
	}

	private void initializenew() {
		frame = new JFrame();
		frame.setBounds(100, 100, 961, 632);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				send_message("exit", null,null,null);
			}
			  });
		frame.getContentPane().setLayout(null);
		frame.setVisible(true);
		
		chatroom = new JTextArea();
		chatroom.setBounds(290, 73, 630, 456);
		chatroom.setEditable(false);
		chatroom.setLineWrap(true);
		frame.getContentPane().add(chatroom);
		
		
		JLabel lblNewLabel = new JLabel("Connected users");
		lblNewLabel.setBounds(82, 30, 167, 33);
		frame.getContentPane().add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Messages ");
		lblNewLabel_1.setBounds(374, 30, 140, 26);
		frame.getContentPane().add(lblNewLabel_1);
		
		msg = new JTextField();
		msg.setBounds(290, 541, 404, 26);
		frame.getContentPane().add(msg);
		msg.setColumns(10);
		
		JPanel panel = new JPanel();
		panel.setBounds(42, 73, 236, 352);
		frame.getContentPane().add(panel);
		
		listModel = new DefaultListModel<String>();
		JList list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		panel.add(list);

		JButton btnNewButton_3 = new JButton("Send Voice");
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (btnNewButton_3.getText().equals("Send Voice")) {
					btnNewButton_3.setText("End voice message");
					//add recording of the voice message here and save in global variable voice file
					Client_conn c = new Client_conn(null, null, null, 1, null);
					c.start();
				} else {
					if (list.isSelectionEmpty()) {
						endvn = true;
						JOptionPane.showMessageDialog(null, "Select a users to Send voice message", "InfoBox: ", JOptionPane.INFORMATION_MESSAGE);
				   } else {
						endvn = true;
						try {
							voicefile = Files.readAllBytes(Paths.get("./soundbytes.txt"));
							File f = new File("./soundbytes.txt");
							f.delete();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					   btnNewButton_3.setText("Send Voice");
					   Voice_message temp = new Voice_message(list.getSelectedValue().toString().split("\\(")[0],voicefile);
					   send_message("VN",null,null,temp);
					   endvn = false;
					   list.clearSelection();
				   }
				}
				}
		});
		btnNewButton_3.setBounds(52, 500, 171, 25);
		frame.add(btnNewButton_3);

	    btnNewButton = new JButton("Call users");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (btnNewButton.getText().equals("Call users")) {
					if (list.isSelectionEmpty()) {
						JOptionPane.showMessageDialog(null, "Select a users to Call", "InfoBox: ", JOptionPane.INFORMATION_MESSAGE);
				   } else {
					   ArrayList<Users> calling = new ArrayList<Users>();
	
					   int[] selectedIndices = list.getSelectedIndices();
					   for (int i = 0; i < selectedIndices.length; i++) {
						   String name = String.valueOf(list.getModel().getElementAt(selectedIndices[i]));
						   name = name.split("\\(")[0];
						   Users temp = new Users(name,true);
						   calling.add(temp);
						   System.out.println("this is the calling "+ name);
					   }
					   
					   Users temp = new Users(my_hostname,true);
					   calling.add(temp);
					   send_message("",null,calling,null);
					   list.clearSelection();
					   msg.setText("");
				   }
				} else {
					System.out.println("gets into correct part");
					send_message("End",null,null,null);
					
				}
				}
				
		});
		btnNewButton.setBounds(749, 558, 171, 25);
		frame.getContentPane().add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("Direct message");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (list.isSelectionEmpty()) {
					 JOptionPane.showMessageDialog(null, "Select a user to direct message", "InfoBox: ", JOptionPane.INFORMATION_MESSAGE);
				} else {
					Direct_message drcmsg = new Direct_message(clientusername, list.getSelectedValue().toString().split("\\(")[0], msg.getText());
					send_message("direct_msg",drcmsg,null,null);
					list.clearSelection();
					msg.setText("");
				}
			}
		});
		btnNewButton_1.setBounds(52, 558, 167, 25);
		frame.getContentPane().add(btnNewButton_1);
	}

	public static void redraw(Client_obj pattern) {
		chatroom.setText("");
		listModel.clear();
		String avail = null;
		for (Users users : pattern.get_users()) {
			if (users.get_Available()){
				avail = "Available";
		   } else {
			   avail = "In call";
		   }
			listModel.addElement(users.get_Username()+"("+avail+")");
		}
		for (Direct_message dr : pattern.get_dm()) {
			System.out.println("it does go in here");
			System.out.println(dr.get_to());
			if (dr.get_to().equals(my_hostname)) {
				
				//JOptionPane.showMessageDialog(frame,, "Wisper message from "+dr.get_from(), JOptionPane.INFORMATION_MESSAGE);
				chatroom.append(dr.get_msg()+" (from "+dr.get_from()+")\n");
			}
		}
	}
	public static void toggleCall(boolean endcall) {
		if (endcall) {
			btnNewButton.setText("End call");
		} else {
			btnNewButton.setText("Call users");
		}

	}
	public static void playmessage(Voice_message vn) {
		if (vn.getToUsername().equals(my_hostname)) {
			int read = JOptionPane.showConfirmDialog(frame, "Listen to voice message","test",JOptionPane.YES_NO_OPTION);
			if (read == JOptionPane.YES_OPTION) {
				//play the sound of vn 
				Client_conn c = new Client_conn(null, null, null, 2, vn.getVoice());
				c.start();
			}
		}
	}
	public static void serverclose() {
		JOptionPane.showMessageDialog(null, "Disconected from server client will terminate", "Server Disconnect ", JOptionPane.INFORMATION_MESSAGE);
	}

}
// Exception in thread "Thread-0" java.lang.IllegalArgumentException: Line unsupported: interface SourceDataLine supporting format PCM_SIGNED 8000.0 Hz, 8 bit, mono, 1 bytes/frame, 
	// at java.desktop/com.sun.media.sound.DirectAudioDevice.getLine(DirectAudioDevice.java:175)
	// at Client_conn.get_sdl(Client_conn.java:182)
	// at Client_conn.setup_sound(Client_conn.java:133)
	// at Client_conn.run(Client_conn.java:45)

