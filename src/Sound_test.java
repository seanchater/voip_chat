import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Vector;

import javax.sound.sampled.*;

public class Sound_test {
	public static TargetDataLine tdl = null;
	public static SourceDataLine sdl = null;
	public static SourceDataLine sdl2 = null;
	public static Mixer.Info[] mixinfo;
	public static AudioFormat af;
	public static ByteArrayOutputStream out;

	public static void main(String[] args) {
		float sampleRate = 16100.0F;
		int sampleSizeInBits = 16;
		int channels = 2;
		boolean signed = true;
		boolean bigEndian = false;
		af = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);

		System.out.println("Getting the machines mixers:");
		mixinfo = AudioSystem.getMixerInfo();
		for (int cnt = 0; cnt < mixinfo.length; cnt++) {
            System.out.println(mixinfo[cnt].getName());
        }
		System.out.println();

		Mixer m = AudioSystem.getMixer(null);
		DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class, af);
		ArrayList<SourceDataLine> freeLines = new ArrayList<SourceDataLine>();
		for (int i = 0; i < 3; i++) {
		  try {
			SourceDataLine line = (SourceDataLine) m.getLine(lineInfo);
			freeLines.add(line);
		  } catch (LineUnavailableException e) {
			System.out.println("Failed to get line from mixer");
			e.printStackTrace();
		  }
		}
		DataLine.Info tdli = new DataLine.Info(TargetDataLine.class, af);
		ArrayList<TargetDataLine> tfreeLines = new ArrayList<TargetDataLine>();
		for (int i = 0; i < 3; i++) {
		  try {
			TargetDataLine line = (TargetDataLine) m.getLine(tdli);
			tfreeLines.add(line);
		  } catch (LineUnavailableException e) {
			System.out.println("Failed to get line from mixer");
			e.printStackTrace();
		  }
		}

		System.out.println("source dl");
		Line[] srcs = new Line[freeLines.size()];
		for (int i = 0; i < freeLines.size(); i++) {
			System.out.println(freeLines.get(i).getClass());
			srcs[i] = freeLines.get(i);
		}
		System.out.println("target dl");
		for (TargetDataLine t: tfreeLines) {
			System.out.println(t.getClass());
		}
		System.exit(1);


		DataLine.Info sinfo = new DataLine.Info(SourceDataLine.class, af);
		// System.out.println(sinfo);
		try {
			sdl = (SourceDataLine) m.getLine(sinfo); //(SourceDataLine) AudioSystem.getLine(sinfo);
			sdl2 = (SourceDataLine) m.getLine(sinfo);
		} catch (LineUnavailableException e1) {
			e1.printStackTrace();
		}
 
		// Checking system sound stuff
		// formats();
		// Vector<AudioFormat> formats = getFormats(SourceDataLine.class);
		// af = formats.get(0);
		// System.exit(0);

		// set up sound input
		DataLine.Info target_info = new DataLine.Info(TargetDataLine.class, af);
		if (!AudioSystem.isLineSupported(target_info)) {
			System.out.println("target_info dataline not supported");
			System.exit(1);
		} else {
			System.out.println("target_info supported");
		}
		// get the target data line from the microphone
		try {
			// old way tdl = (TargetDataLine) mic_mixer.getLine(target_info);
			tdl = (TargetDataLine) AudioSystem.getLine(target_info);
			tdl.open(af);
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
		// set up sound output
		// DataLine.Info source_info = new DataLine.Info(SourceDataLine.class, af);
		// Mixer out_mixer = AudioSystem.getMixer(mixinfo[1]);
		try {
			// sdl = (SourceDataLine) AudioSystem.getLine(source_info);
			sdl.open(af);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		
		// ************************* record and play *************************r

		String file1 = "soundbytes_1.txt", file2 = "soundbytes_2.txt";
		listen(5, file1);
		listen(5, file2);
		speak(file1, file2);
		
	
	}

	public static void listen(int listen_time, String filename) {
		// ************************** listen on mic for 5 seconds *************************
		System.out.println("listening");
		int numBytesRead;
		byte[] data = new byte[tdl.getBufferSize() / 5];
		// Begin audio capture from mic
		tdl.start();
		// Here, stopped is a global boolean set by another thread.
		try (FileOutputStream fos = new FileOutputStream("./" + filename)) {
			long stop_time = System.currentTimeMillis() + (listen_time * 1000);
			while (System.currentTimeMillis() < stop_time) {
				// Read the next chunk of data from the TargetDataLine.
				numBytesRead = tdl.read(data, 0, data.length);
				// Save this chunk of data.
				fos.write(data, 0, numBytesRead);
			}     
		 } catch (IOException e) {
			e.printStackTrace();
		}
		tdl.stop();
	}

	public static void speak(String file1, String file2) {
		// ************************* output to speaker *************************r
		System.out.println("playing");
		sdl.start();
		byte[] array, array2;
		try {
			array = Files.readAllBytes(Paths.get("./" + file1));
			array2 = Files.readAllBytes(Paths.get("./" + file2));
			sdl.write(array, 0, array.length);
			sdl2.write(array2, 0, array2.length);
		} catch (IOException e) {
			e.printStackTrace();
		}
			sdl.drain();
			sdl.close();
	}

	public static void formats() {
		Mixer mic_mixer = AudioSystem.getMixer(null);
		try {
			mic_mixer.open();
		} catch (LineUnavailableException e1) {
			e1.printStackTrace();
		}

		System.out.printf("Supported SRCDATALINE of default mixer (%s):\n\n", mic_mixer.getMixerInfo().getName());
		for(Line.Info info : mic_mixer.getSourceLineInfo()) {
			if(SourceDataLine.class.isAssignableFrom(info.getLineClass())) {
				SourceDataLine.Info info2 = (SourceDataLine.Info) info;
				System.out.println(info2);
				System.out.printf("  max buffer size: \t%d\n", info2.getMaxBufferSize());
				System.out.printf("  min buffer size: \t%d\n", info2.getMinBufferSize());
				AudioFormat[] formats = info2.getFormats();
				System.out.println("  Supported Audio formats: ");
				for(AudioFormat format : formats) {
					System.out.println("    "+format);
		         System.out.printf("      encoding:           %s\n", format.getEncoding());
		         System.out.printf("      channels:           %d\n", format.getChannels());
		         System.out.printf(format.getFrameRate()==-1?"":"      frame rate [1/s]:   %s\n", format.getFrameRate());
		         System.out.printf("      frame size [bytes]: %d\n", format.getFrameSize());
		         System.out.printf(format.getSampleRate()==-1?"":"      sample rate [1/s]:  %s\n", format.getSampleRate());
		         System.out.printf("      sample size [bit]:  %d\n", format.getSampleSizeInBits());
		         System.out.printf("      big endian:         %b\n", format.isBigEndian());
		         
		        //  Map<String,Object> prop = format.properties();
		        //  if(!prop.isEmpty()) {
		        //      System.out.println("      Properties: ");
		        //      for(Map.Entry<String, Object> entry : prop.entrySet()) {
		        //          System.out.printf("      %s: \t%s\n", entry.getKey(), entry.getValue());
		        //      }
		        //  }
				}
				System.out.println();
			} else {
				System.out.println(info.toString());
			}
			System.out.println();
		}

		mic_mixer.close();
	}

	public static Vector<AudioFormat> getFormats(Class<?> dataLineClass) {
		float samplerates[] = { (float) 8000.0, (float) 16000.0, (float) 44100.0};
		int channels[] = {1, 2};
		int bytespersamplep[] = {2};

		AudioFormat format;
		DataLine.Info lineinfo;

		// SystemAudioProfile profile = new SystemAudioProfile();
		Vector<AudioFormat> formats = new Vector<AudioFormat>();

		for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
				System.out.println("mixer name: " + mixerInfo.getName());
				System.out.println("mixer description : " + mixerInfo.getDescription());
			for (int a = 0; a < samplerates.length; a++) {
				for (int b = 0; b < channels.length; b++) {
					for (int c  = 0; c < bytespersamplep.length; c++) {
						format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
								samplerates[a], 8 * bytespersamplep[c], channels[b], bytespersamplep[c], 
								samplerates[a], false);
						lineinfo = new DataLine.Info(dataLineClass, format);
						if (AudioSystem.isLineSupported(lineinfo)) {
							if (AudioSystem.getMixer(mixerInfo).isLineSupported(lineinfo)) {
								formats.add(format);
								System.out.println(format);
							}
						}
					}
				}
			}
								System.out.println();
		}
		return formats;
	}

	private class Listen extends Thread {

		public void run() {
			// out  = new ByteArrayOutputStream();
			
			int numBytesRead;
			byte[] data = new byte[tdl.getBufferSize() / 5];
			// Begin audio capture from mic
			tdl.start();

			// Here, stopped is a global boolean set by another thread.
			while (true) {
				// Read the next chunk of data from the TargetDataLine.
				numBytesRead = tdl.read(data, 0, data.length);
				// Save this chunk of data.
				try (FileOutputStream fos = new FileOutputStream("./soundbytes.txt")) {
					fos.write(data, 0, numBytesRead);
				 } catch (IOException e) {
					e.printStackTrace();
				}
				// out.write(data, 0, numBytesRead);
			}     
		}
	}

	private class Speak extends Thread {

		public void run() {

			sdl.start();
			byte[] array;
			try {
				array = Files.readAllBytes(Paths.get("/path/to/file"));
				sdl.write(array, 0, array.length);
			} catch (IOException e) {
				e.printStackTrace();
			}
				sdl.drain();
				sdl.close();
		}
	}
}