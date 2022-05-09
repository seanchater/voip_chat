
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import java.awt.event.WindowAdapter;

public class Server_Gui {

	private JFrame frame;
	DefaultListModel<String> listModel;
    private JTextArea chatroom;

	/**
	 * Launch the application.
	 */
    public Server_Gui() {
        initialize();
    }

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 961, 632);
		frame.addWindowListener(new WindowAdapter() {
			 
			@Override
			 
			public void windowClosing(WindowEvent e) {
			    System.exit(0);
			}
			 
			  });
		frame.getContentPane().setLayout(null);
		
		chatroom = new JTextArea();
		chatroom.setBounds(290, 73, 630, 456);
		chatroom.setEditable(false);
		chatroom.setLineWrap(true);
		frame.getContentPane().add(chatroom);
		
		JLabel lblNewLabel = new JLabel("Connected users");
		lblNewLabel.setBounds(82, 30, 167, 33);
		frame.getContentPane().add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Information ");
		lblNewLabel_1.setBounds(374, 30, 140, 26);
		frame.getContentPane().add(lblNewLabel_1);
		
		JPanel panel = new JPanel();
		panel.setBounds(42, 73, 236, 352);
		frame.getContentPane().add(panel);
		
		listModel = new DefaultListModel<String>();
		JList list = new JList(listModel);
		panel.add(list);
		
	}
    public void setVis() {
        frame.setVisible(true);
    }
    public void UpdateUsers(ArrayList<Users> pattern) {
        listModel.clear();
		String avail=null;
		for (Users users : pattern) {
			if (users.get_Available()){
			 	avail = "Available";
			} else {
				avail = "In call";
			}
			listModel.addElement(users.get_Username()+"("+avail+")");
		}
    }
    public void appendMessage(String messages){
        chatroom.append(messages);
    }
    
}
