import java.io.*;
import java.net.*;
import java.util.*;

/*
*/

public class cdht {

	private static final int PORTNUM = 50000;
	private static final int DELAY = 10000;
	private static final int TIMEOUT = 2000;

	private static class UDPServer implements Runnable {

		DatagramSocket socket = null;

		public UDPServer(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			while (true) {
				DatagramPacket packet = socket.receive();
				new Thread(new UDPHandler(socket, packet)).start();
			}
		}
	}	

	private static class UDPHandler implements Runnable {
		
	}	

	private static class SendPing extends Thread {
		int receiverID;
		DatagramSocket socket;

		SendPing(int receiverID, DatagramSocket socket) {
			this.receiverID = receiverID;
			this.socket = socket;
		}

		public void run() {
			String msg = "PING";
			byte[] buf = new byte[1024];
			buf = msg.getBytes();

			InetAddress server;
			try {
				while (true) {
					server = InetAddress.getLocalHost();

					int port = PORTNUM + this.receiverID;

					//DatagramSocket socket = new DatagramSocket();
		
					//Create datagram
					DatagramPacket ping = new DatagramPacket(buf, buf.length, server, port);
					socket.send(ping);

					socket.setSoTimeout(TIMEOUT);
					DatagramPacket reply = new DatagramPacket(new byte[1024], 1024);
					socket.receive(reply);

					System.out.println("A ping response message was received from Peer " + this.receiverID + ".");
				}
			} catch (SocketTimeoutException e) {
				System.out.println("Timeout peer: " + this.receiverID);
			} catch (UnknownHostException e) {
				System.out.println("Unknown host.");
			} catch (Exception e) {
				e.printStackTrace();
			}				
		}
	}

	private static class ListenPing extends Thread {
		int ID;
		DatagramSocket socket;

		ListenPing(DatagramSocket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				// int port = PORTNUM + this.ID;
				// DatagramSocket socket = new DatagramSocket(port);

				while (true) {
					DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
					socket.setSoTimeout(0);
					socket.receive(request);

					InetAddress clientHost = request.getAddress();
					int clientPort = request.getPort();

					int senderID = clientPort - PORTNUM;
					System.out.println("A ping request message was received from Peer " + senderID + ".");

					byte[] buf = request.getData();
					DatagramPacket reply = new DatagramPacket(buf, buf.length, clientHost, clientPort);
					socket.send(reply);
				}						
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private static int hash(int filename) {
		return filename % 256;
	}

	// public void run() {
	// 	System.out.println("Start peer");
	// }

	public static void main(String[] args) throws Exception {
		// Arguments error
		if (args.length != 3) {
			System.out.println("Uses: cdht port 1st_succesor_ID 2nd_successor_ID");
			return;
		}
		int ID = Integer.parseInt(args[0]); //node ID
		int fID = Integer.parseInt(args[1]); // First successor ID
		int sID = Integer.parseInt(args[2]); // Second successor ID
		DatagramSocket socket = new DatagramSocket(ID + PORTNUM);

		SendPing s1 = new SendPing(fID, socket);
		SendPing s2 = new SendPing(sID, socket);
		ListenPing l = new ListenPing(socket);

		s1.start();
		s2.start();
		l.start();				
	}
}