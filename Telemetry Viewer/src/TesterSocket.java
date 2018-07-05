import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TesterSocket {

	private static Thread transmitter;
	private static Thread receiver;
	
	private static DatagramSocket socket;
	private static InetAddress address;

	private static DataInputStream dataInStream;
	
	public static void startSocketServer() {
		try {
			socket = new DatagramSocket();
			address = InetAddress.getByName("localhost");
			
			// receive continuously
			startReceive();
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public static DatagramSocket getSocket() {
		return socket;
	}

	public static DataInputStream getInputStream() {
		return dataInStream;
	}
	
	public void close() {
		socket.close();
	}

	
	public static void startReceive() {
		receiver = new Thread(new Runnable() {
			@Override public void run() {

				//https://stackoverflow.com/questions/36067414/datainputstream-over-datagramsocket
				byte[] buffer = new byte[2048];
				DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
				
				dataInStream = new DataInputStream(new ByteArrayInputStream(dp.getData(), dp.getOffset(), dp.getLength()));
				
				while(true) {

						try {
							socket.receive(dp);
							
							Thread.sleep(1);
							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
				}
				
			}
		});
		
		receiver.setPriority(Thread.MAX_PRIORITY);
		receiver.setName("Test Socket Recv");
		receiver.start();
	}
	
	
	/**
	 * Simulates the transmission of 4 numbers every 100us.
	 * The first three numbers are pseudo random, and scaled to form a sort of sawtooth waveform.
	 * The fourth number is a 1kHz sine wave.
	 * This is used to check for proper autoscaling of charts, etc.
	 */
	public static void startTransmission() {
		
		transmitter = new Thread(new Runnable() {
			@Override public void run() {

				byte[] buf;
				
				while(true) {
					
					try {
						
						String msg = Double.toString(Math.random()) + "," + 
								Double.toString(Math.random()) + System.lineSeparator();
						
						buf = msg.getBytes();
						DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);
						socket.send(packet);
						
						Thread.sleep(1);
						
					} catch(InterruptedException e) {
						
						// stop and end this thread if we get interrupted
						return;
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		});
		transmitter.setPriority(Thread.MAX_PRIORITY);
		transmitter.setName("Test Socket Transmitter");
		transmitter.start();
		
	}
	
	public static void stopTransmission() {
		
		if(transmitter != null && transmitter.isAlive()) {
			transmitter.interrupt();
			while(transmitter.isAlive()); // wait
		}
		
		// and receiver
		if(receiver != null && receiver.isAlive()) {
			receiver.interrupt();
			while(receiver.isAlive()); // wait
		}
		
	}
	
	public static void populateDataStructure() {
		
		Controller.removeAllDatasets();
		
		int location = 0;
		BinaryFieldProcessor processor = BinaryPacket.getBinaryFieldProcessors()[0];
		String name = "";
		Color color = null;
		String unit = "Volts";
		float conversionFactorA = 1;
		float conversionFactorB = 1;
		
		location = 0;
		name = "Waveform A";
		color = Color.RED;
		Controller.insertDataset(location, processor, name, color, unit, conversionFactorA, conversionFactorB);
		
		location = 1;
		name = "Waveform B";
		color = Color.GREEN;
		Controller.insertDataset(location, processor, name, color, unit, conversionFactorA, conversionFactorB);
		
	}

}
