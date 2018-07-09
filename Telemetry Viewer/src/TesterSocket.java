import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.UIManager;

public class TesterSocket {

	private static Thread transmitter;
	private static Thread receiver;
	
	private static DatagramSocket socketServer;
	private static DatagramSocket socketClient;
	private static InetAddress address;
	private static int port = 9999;
	
	private static ByteArrayInputStream 	bais;
	
	public static void startSocketServer() {
		try {
			address = InetAddress.getLocalHost();
			
			socketServer = new DatagramSocket(port,address); // port on localhost
			socketClient = new DatagramSocket(6969);

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void close() {
		//socket.close();
	}

	public static ByteArrayInputStream getInputStream() {
		return bais;
	}
	
	public static void startReceive() {
		receiver = new Thread(new Runnable() {
			@Override public void run() {

				//https://stackoverflow.com/questions/36067414/datainputstream-over-datagramsocket
				byte[] buffer = new byte[256];
				DatagramPacket			dp 				= new DatagramPacket(buffer, buffer.length);
				bais = new ByteArrayInputStream(dp.getData(), dp.getOffset(), dp.getLength());
				//InputStreamReader		isr				= new InputStreamReader(bais);
				//BufferedReader 			reader 			= new BufferedReader(isr);
				
				while(true) {

						try {
							socketServer.receive(dp);
							
							//dataInStream = /*new DataInputStream(*/
							//		new ByteArrayInputStream(dp.getData(), dp.getOffset(), dp.getLength());
							//while(reader.ready())
							//	Thread.sleep(1);
							//while(reader.ready()) {
							//	System.out.println(reader.readLine());
							//}
							//bais.reset();
							//Thread.sleep(1);
							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
				}
				
			}
		});
		
		//receiver.setPriority(Thread.MAX_PRIORITY);
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
				
				int counter = 0; 
				String msg = "";
				
				while(true) {
					
					try {
						msg = "0.001," + Integer.toString(counter) + System.lineSeparator();
						byte[] buf = msg.getBytes();
			
						socketClient.send(new DatagramPacket(buf, buf.length, address, port));
						
						counter ++;
						
						Thread.sleep(1);
					
					} catch (IOException | InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		});
		//transmitter.setPriority(Thread.NORM_PRIORITY);
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
	
	public static void main(String[] args) {

		TesterSocket.startSocketServer();
		
		TesterSocket.startReceive();
		TesterSocket.startTransmission();


	}

}
