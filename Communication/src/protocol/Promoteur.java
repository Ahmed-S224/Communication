package protocol;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.Scanner;

import protocol.exceptions.NumberOfBytesException;
import protocol.exceptions.ParseException;

public class Promoteur{
	
	public Thread threadTCP;
	public Thread threadUDP;

	private MulticastSocket sockMulti;
	private Socket sockToServ;

	private String messPROMO;
	private String messPUB;

	String ipServ;
	int portServ;

	
	String ipMulti;
	int portMulti;
	private static Scanner sc;

	public Promoteur(String messPROMO, String messPUB, int portMulti, String ipServ, int portServ) throws IOException {
		super();
		this.messPROMO = messPROMO;
		this.messPUB = messPUB;
		this.portMulti = portMulti;
		try {
			this.ipMulti=Message.convertIP("225.10.12.4");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.ipServ = ipServ;
		this.portServ = portServ;

		this.sockMulti = new MulticastSocket(portMulti);

		this.sockToServ = new Socket(ipServ, portServ);

	}

	public void tourner2() throws IOException{
		
		
		
		while(true){
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			
			Message tmp;
			try {
				tmp = Message.PROM(this.messPROMO);
			} catch (Exception e) {
				break;
			}
			
			
			byte[] msgBytes = tmp.getData();
			
			DatagramPacket p= new DatagramPacket(msgBytes, msgBytes.length,InetAddress.getByName("225.10.12.4"),8888);
			
			this.sockMulti.send(p);

		}
		
	}

	public void sendTCP() throws IOException {

		BufferedOutputStream buffOut;
		BufferedInputStream buffIn;

		buffOut = new BufferedOutputStream(this.sockToServ.getOutputStream());
		buffIn = new BufferedInputStream(this.sockToServ.getInputStream());

		System.out.println("START PROMOTEUR -> SEND TCP");

		while (true) {
			Message msg;
			try {
				msg = Message.PUBL_INTERRO(this.ipMulti, this.portMulti, this.messPUB);

				buffOut.write(msg.getData());
				buffOut.flush();

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NumberOfBytesException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			byte[] tmpMsg2 = new byte[Message.maxSIZEmsg];

			int sizeReturn = buffIn.read(tmpMsg2);

			try {
				Message rsv = Message.parseMessageWithSize(tmpMsg2, sizeReturn);
				
				System.out.println("MSG REVECE : "+rsv.toString());
				
			} catch (Exception e) {
				
				System.out.println("ECHEC PROMOTEUR");
			}

			break;
		}

		this.sockToServ.close();
	}

	public static void main(String[] args) throws IOException, ParseException{
		sc = new Scanner(System.in);
		System.out.println("Choisissez un port entre 5000 et 6000");
		int portMult = sc.nextInt();
		System.out.println("Ecrivez votre message de promotion de diffusion serveur");
		sc.nextLine();
		String mess_pub = sc.nextLine();
		System.out.println("Ecrivez votre message de promotion de diffusion");
		String mess_prom = sc.nextLine();
		Promoteur p = new Promoteur(mess_prom, mess_pub, portMult , "127.0.0.1", 9998);
		Runnable tcp= new Runnable() {
			
			
			public void run() {
				try {
					p.sendTCP();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		Runnable udp= new Runnable() {
			
			
			public void run() {
				try {
					p.tourner2();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		p.threadUDP=new Thread(udp);
		p.threadUDP.start();
		p.threadTCP=new Thread(tcp);
		p.threadTCP.start();
		

	}
}
