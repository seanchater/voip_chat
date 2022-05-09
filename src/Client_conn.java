import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.sound.sampled.*;

/**
 * Listens for incoming chatroom msgs (or direct msgs or new user updates) from the server to update the gui
 */
public class Client_conn extends Thread {

	private Socket socket;
	private ObjectInputStream in;
	private static Listener l;
	private static Speaker s;
	private static boolean in_call;
	private String my_hostname;
	private int vn;
	private File vn_file = null;
	private byte[] vn_bytes;

	public Client_conn(Socket socket, ObjectInputStream in, String my_hostname, int vn, byte[] vn_bytes) {
		this.socket = socket;
		this.in = in;
		this.my_hostname = my_hostname;
		this.vn = vn;
		this.vn_bytes = vn_bytes;
	}

	public void run() {
		if (vn == 1 || vn == 2) {
			// this thread is a once off one for recording a vn and returning it as a file object
			if (!setup_sound(null)) {
				System.out.println("Could not setupt the sound stuff...");
			}	
		} else {
			// this thread is the main thread that listens for incoming messages from server and calls
			// from clients.
			try {
			Client_obj server_resp;
			while (true) {
				// // get response from server
				server_resp = (Client_obj) in.readObject();
				int header = server_resp.get_header();
				
				switch (header) {
					case 1:
						// If someone has asked me to join a call
						String other_hostname = null;
						// TODO: only for two clients calling eachother
						for (Users u : server_resp.get_inCall()) {
							if (!u.get_Username().equals(my_hostname)) {
								other_hostname = u.get_Username();
								break;
							}
						}
						Gui.toggleCall(true);
						if (!setup_sound(other_hostname)) {
							System.out.println("Could not setupt the sound stuff...");
						}
						break;

					case 2:
						// when the call I am currently in has ended
						Gui.toggleCall(false);
						in_call = false;
						break;
					
					case 3:
						// TODO: conference calls
						/** 
							Create an array of all the users in the call
							and send this to the listener and speaker thread constructor.
							Listener thread will then broadcast all of its packets
							to all the IP's specified in the array
							Speaker thread will open a new socket for each of IP's in the
							array and wait for incoming packet -> or just receive all packets on one socket?

						 */
						break;

						case 4:
							// receive a vn and call GUI method to play it
							byte[] vn_data = server_resp.get_voicemes().getVoice();
							OutputStream out = new FileOutputStream(new File("./soundbytes.txt"));
							out.write(vn_data);
							out.close();
							Gui.playmessage(server_resp.get_voicemes());
							break;

					default:
						break;
				}
				Gui.redraw(server_resp);
				/**
				 * use try catch to determine which type of message is coming in 
				 * or use header of message object to determine if next object will
				 * be a client_obj
				 * 
				 * for msgs Switch on header to either:
				 * 		- accept call and then start sound threads and setup socket
				 * 		- reject call and display
				 * 		- 
				 */

				// System.out.println("[RECV] " + server_resp.toString());
				// draw gui
			}
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + socket.getInetAddress());
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " +
				socket.getInetAddress());
			Gui.serverclose();
			System.exit(1);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.err.printf("Message class (%s) not found when reading resp from socket\n", e.getClass());
		} 

		}
		
	}

	public byte[] get_vnfile() {
		byte[] soundbytes = null;
		try {
			soundbytes = Files.readAllBytes(Paths.get("./soundbytes.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return soundbytes;
	}
		
			/**
		Sets up the mixer and gets the datalines for i/o of sound bytes
		Also starts the listener and speaker threads to start sending and 
		receiving sound bytes over UDP sockets
	 */
	private boolean setup_sound(String other_hostname) {
		// System.out.println("Getting the machines mixers:");
		// Mixer.Info mixinfo[] = AudioSystem.getMixerInfo();
		// for (int cnt = 0; cnt < mixinfo.length; cnt++) {
        //     System.out.println(mixinfo[cnt].getName());
        // }

		// set the desired audio format 
		float sampleRate = 16000.0F;
		int sampleSizeInBits = 16;
		int channels = 2;
		boolean signed = true;
		boolean bigEndian = false;
		AudioFormat af = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);


		// get the datalines for I/O
		TargetDataLine tdl = get_tdl(af);
		if (tdl == null) {
			System.out.println("Could not get TDL");
			return false;
		}
		try {
			tdl.open(af);
		} catch (LineUnavailableException e) {
			System.err.println("Could not open TDL");
			e.printStackTrace();
		}

		SourceDataLine sdl = get_sdl(af);
		if (sdl == null) {
			System.out.println("Could not get SDL");
			return false;
		}
		try {
			sdl.open(af);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			System.err.println("Could not open SDL");
		}

		// setup the UDP socket
		
		// start the mic listen and speaker output threads
		in_call = true;
		if (vn == 0) {
			l = new Listener(tdl, other_hostname);
			s = new Speaker(sdl, other_hostname);
			l.start();
			s.start();
		} else if (vn == 1) {
			l = new Listener(tdl, other_hostname);
			l.start();
		} else if (vn == 2) {
			s = new Speaker(sdl, other_hostname);
			s.start();
		}

		return true;
	}


	private static TargetDataLine get_tdl(AudioFormat af) {
		// set up sound input
		// Mixer mic_mixer = AudioSystem.getMixer(mixinfo[1]);
		DataLine.Info target_info = new DataLine.Info(TargetDataLine.class, af);
		if (!AudioSystem.isLineSupported(target_info)) {
			System.out.println("target_info dataline not supported");
			System.exit(1);
		} else {
			System.out.println("target_info dataline supported");
		}
		// get the target data line from the microphone
		try {
			return (TargetDataLine) AudioSystem.getLine(target_info);
			// return (TargetDataLine) mic_mixer.getLine(target_info);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static SourceDataLine get_sdl(AudioFormat af) {
		// set up sound output
		// Mixer speaker_mixer = AudioSystem.getMixer(mixinfo[2]);
		DataLine.Info source_info = new DataLine.Info(SourceDataLine.class, af);
		if (!AudioSystem.isLineSupported(source_info)) {
			System.out.println("source dataline not supported");
			System.exit(1);
		} else {
			System.out.println("source dataline supported");
		}
		try {
			return (SourceDataLine) AudioSystem.getLine(source_info);
			// return (SourceDataLine) speaker_mixer.getLine(source_info);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			return null;
		}
	}

	private class Listener extends Thread {

	private TargetDataLine tdl;
	private DatagramSocket sock;
	private String other_addr;


	public Listener(TargetDataLine tdl, String addr) {
		this.tdl = tdl;
		this.other_addr = addr;
	}

	public void run() {
		
		byte[] data = new byte[4096];
			// Begin audio capture from mic
			tdl.start();

			if (vn == 1) {
				vn_file = new File("./soundbytes.txt");
				try (FileOutputStream fos = new FileOutputStream(vn_file)) {
					while (!Gui.endvn) {
						// Read the next chunk of data from the TargetDataLine.
						int numBytesRead = tdl.read(data, 0, data.length);
						// Save this chunk of data.
						fos.write(data, 0, numBytesRead);
					}     
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (vn == 0) {
				DatagramSocket sock = null;
				try {
					sock = new DatagramSocket(5000);
					// Here, stopped is a global boolean set by another thread.
					while (in_call) {
						// Read the next chunk of data from the TargetDataLine.
						tdl.read(data, 0, data.length);
						// write to the socket
						DatagramPacket pk = new DatagramPacket(data, data.length, InetAddress.getByName(other_addr), 5001);
						sock.send(pk);
					}     
				} catch (SocketException e1) {
					e1.printStackTrace();
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				// end call
				sock.close();
			} else {
				return;
			}
			System.out.println("listener ending");
			tdl.stop();
			tdl.close();
	}
}

private class Speaker extends Thread {

	private SourceDataLine sdl;
	private String other_addr;
	/**
         * Formula for lag = (byte_size/sample_rate)*2
		 In our case:
		 	byte size = 1024
			sample rate = 8000
			therefore lag = 0.256 seconds of lag
         * Byte size 9728 will produce ~ 0.45 seconds of lag. Voice slightly broken.
         * Byte size 1400 will produce ~ 0.06 seconds of lag. Voice extremely broken.
         * Byte size 4000 will produce ~ 0.18 seconds of lag. Voice slightly more broken then 9728.
         */

	public Speaker(SourceDataLine sdl, String addr) {
		this.sdl = sdl;
		this.other_addr = addr;
	}

	public void run() {

		sdl.start();
		if (vn == 2) {
			sdl.start();
			byte[] array;
			try {
				array = Files.readAllBytes(Paths.get("./soundbytes.txt"));
				sdl.write(array, 0, array.length);
			} catch (IOException e) {
				e.printStackTrace();
			}
			File f = new File("./soundbytes.txt");
			f.delete();
		} else if (vn == 0) {
			byte[] array = new byte[4096];

			DatagramSocket sock = null;
			try {
				sock = new DatagramSocket(5001);
				DatagramPacket pk = new DatagramPacket(array, array.length);
				while (in_call) {
					sock.receive(pk);
					sdl.write(pk.getData(), 0, pk.getData().length);
				}
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// end call
			sock.close();
		} else {
			return;
		}
		System.out.println("speaker ending");
		sdl.stop();
		sdl.drain();
		sdl.close();
	}
}

}