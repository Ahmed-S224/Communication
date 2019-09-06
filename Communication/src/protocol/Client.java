package protocol;

import java.net.*;
import java.util.Scanner;

import protocol.exceptions.NumberOfBytesException;
import protocol.exceptions.ParseException;
import protocol.exceptions.UnknownTypeMesssage;

import java.io.*;

public class Client {
	
	
	public Thread threadTCP;
	public Thread threadUDP;

	private Socket sockTCP;

	private DatagramSocket sockUDP;
	
	private int portUdp;

	private MulticastSocket mso;
	private Scanner sc;

	int portListenMULTI;
	private static Scanner sc2;

	public Client(String ip, int portTCP, int portUDP) throws UnknownHostException, IOException {
		this.portUdp=portUDP;
		this.sockTCP = new Socket(ip, portTCP);
		this.sockUDP = new DatagramSocket(portUDP);
	}

	public void tourner3() throws IOException {

		portListenMULTI = 8888;

		this.mso = new MulticastSocket(portListenMULTI);
		mso.joinGroup(InetAddress.getByName("225.10.12.4"));
		byte[] tmpMsg = new byte[Message.maxSIZEmsg];
		DatagramPacket paquet = new DatagramPacket(tmpMsg, tmpMsg.length);
		while (true) {
			mso.receive(paquet);
			String st = new String(paquet.getData(), 0, paquet.getLength());
			System.out.println("J'ai reçu :" + st);
		}
	}

	public void udpCommunication() throws IOException {

		while (true) {

			byte[] tmpMsg = new byte[Message.maxSIZEmsg];

			DatagramPacket p = new DatagramPacket(tmpMsg, Message.maxSIZEmsg);
			this.sockUDP.receive(p);

			try {
				Message m = Message.parseMessageWithSize(tmpMsg, p.getLength());
				System.out.println("Message UDP recu :" + m.toString());
			} catch (Exception e) {
			}

			
		}

	}

	@SuppressWarnings("deprecation")
	public void tcpCommunication() throws IOException {

		BufferedOutputStream buffOut;
		BufferedInputStream buffIn;


		System.out.println("START CLIENT");

		sc = new Scanner(System.in);
		System.out.println("Bienvenu dans notre réseau de communication.");
		System.out.println("Pour vous connecter taper 1");
		System.out.println("Pour vous vous inscrire taper 2");
		int val = sc.nextInt();
		while (true) {

			buffOut = new BufferedOutputStream(this.sockTCP.getOutputStream());
			buffIn = new BufferedInputStream(this.sockTCP.getInputStream());
			
			if (val == 2) {
				sc = new Scanner(System.in);
				System.out.println("Pour vous inscrir taper de votre identifiant suivi de votre port et mot de passe");
				Message msg;

				try {
					System.out.println("Taper de votre idantifiant :");
					String id = sc.nextLine();
					System.out.println("Taper de votre mot de passe :");
					int mdp = sc.nextInt();
					msg = Message.REGIS(id, this.portUdp, (char) mdp);

					buffOut.write(msg.getData());
					buffOut.flush();

					// TODO test reponse

					byte[] tmpMsg2 = new byte[Message.maxSIZEmsg];

					int sizeReturn = buffIn.read(tmpMsg2);
					Message msgRCV = Message.parseMessageWithSize(tmpMsg2, sizeReturn);

					System.out.println("REPONSE RECU : " + msgRCV.toString());

					val = 3;

				} catch (ParseException e) {
					System.out.println("PAS GOOD");
				} catch (NumberOfBytesException e) {
					System.out.println("PAS GOOD");
				} catch (UnknownTypeMesssage e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (val == 1) {
				sc = new Scanner(System.in);
				System.out.println("Pour vous connecter taper de votre idantifiant et mot de passe");
				Message msg;
				try {
					System.out.println("Taper de votre idantifiant :");
					String id = sc.nextLine();
					System.out.println("Taper de votre mot de passe :");
					int mdp = sc.nextInt();
					msg = Message.CONNE(id, (char) mdp);
					buffOut.write(msg.getData());
					buffOut.flush();

					byte[] tmpMsg2 = new byte[Message.maxSIZEmsg];

					int sizeReturn = buffIn.read(tmpMsg2);
					Message msgRCV = Message.parseMessageWithSize(tmpMsg2, sizeReturn);
					System.out.println("REPONSE RECU : " + msgRCV.toString());
					if (msgRCV.getType() == TypeMessage.GOBYE) {
						val = 1;
						continue;
					} else if (msgRCV.getType() == TypeMessage.HELLO) {
						val = 3;
						continue;
					} else {
						// TODO
					}

				} catch (ParseException e) {
					System.out.println("PAS GOOD");
				} catch (NumberOfBytesException e) {
					System.out.println("PAS GOOD");
				} catch (UnknownTypeMesssage e) {

					e.printStackTrace();
				}

			} else if (val == 3) {

				sc = new Scanner(System.in);
				System.out.println("Pour ajouter un ami tapez \"ajouter\"");
				System.out.println("Pour consulter vos notifs tapez \"consu\"");
				System.out.println("Pour envoyer un message taper \"message\"");
				System.out.println("Pour deconnecter un message taper \"deco\"");
				System.out.println("Pour une inondation de messages taper \"inonder\"");
				System.out.println("Pour une liste de clients  \"liste\"");
				
				
				String requete = sc.nextLine();
				if (requete.equals("ajouter")) {

					try {
						System.out.println("Taper de votre idantifiant :");
						String id = sc.nextLine();

						Message msg = Message.FRIE_INTERRO(id);
						buffOut.write(msg.getData());
						buffOut.flush();

						byte[] tmpMsg2 = new byte[Message.maxSIZEmsg];

						int sizeReturn = buffIn.read(tmpMsg2);
						Message msgRCV = Message.parseMessageWithSize(tmpMsg2, sizeReturn);

						System.out.println("REPONSE RECU : " + msgRCV.toString());

					} catch (ParseException e1) {
					} catch (NumberOfBytesException e1) {
					} catch (UnknownTypeMesssage e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				
				if (requete.equals("deco")) {
					System.out.println("JE ME DECO");
					try {
						Message msg = Message.IQUIT();
						buffOut.write(msg.getData());
						buffOut.flush();
						
						byte[] tmpMsg2 = new byte[Message.maxSIZEmsg];

						int sizeReturn = buffIn.read(tmpMsg2);
						Message msgRCV;
						msgRCV = Message.parseMessageWithSize(tmpMsg2, sizeReturn);

						System.out.println("REPONSE RECU : " + msgRCV.toString());
						
						if (msgRCV.getType() == TypeMessage.GOBYE) {
							this.sockTCP.close();
							this.threadUDP.stop();
							
							break;
						}else{
							//LE SERVEUR NE REPONT PAS BIEN
						}
						
					} catch (Exception e) {
						System.out.println("un problem avec IQUIT");
						e.printStackTrace();
					}
					
				}
				if (requete.equals("message")) {

					try {
						System.out.println("Taper idantifiant de ami :");
						String id = sc.nextLine();
						
						System.out.println("Tapez le nombre de messages que vous souhaitez envoyer");
						int num_mess =sc.nextInt();
						
						Message msg = Message.MESS_INTERRO(id, num_mess);
						
						buffOut.write(msg.getData());
						buffOut.flush();
										
						
						
						String mess="";
						for(int i=1;i<=num_mess;i++){
							
							while(mess.length()==0){
								System.out.println("Taper le texte du message "+i);
								mess = sc.nextLine();
							}
							
							Message msgNumber = Message.MENUM(i+1, mess);
							buffOut.write(msgNumber.getData());
							buffOut.flush();
							mess="";
						}
						
						
						byte[] tmpMsg2 = new byte[Message.maxSIZEmsg];

						int sizeReturn = buffIn.read(tmpMsg2);
						Message msgRCV = Message.parseMessageWithSize(tmpMsg2, sizeReturn);
						System.out.println("REPONSE RECU : " + msgRCV.toString());
						

					} catch (ParseException e1) {
					} catch (NumberOfBytesException e1) {
					} catch (UnknownTypeMesssage e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				if (requete.equals("inonder")) {

					try {
						System.out.println("Taper de votre Message :");
						String mess = sc.nextLine();
						
						Message msg = Message.FLOO_INTERRO(mess);
						
						buffOut.write(msg.getData());
						buffOut.flush();
						

					} catch (ParseException e1) {
					} catch (NumberOfBytesException e1) {
					} 

				}
				if (requete.equals("consu")) {

					try {
						Message msg = Message.CONSU();
						
						buffOut.write(msg.getData());
						buffOut.flush();
						
						byte[] tmpMsg2 = new byte[Message.maxSIZEmsg];

						int sizeReturn = buffIn.read(tmpMsg2);
						Message msgRCV = Message.parseMessageWithSize(tmpMsg2, sizeReturn);

						System.out.println("REPONSE RECU : " + msgRCV.toString());
						if(msgRCV.getType() == TypeMessage.EIRFsup){
							System.out.println("VOULEZ VOUS ETRE AMIC AVEC : " + msgRCV.getId()+" | oui ou non");
							String reponse = sc.nextLine();
							if(reponse.equals("oui")){
								Message confirm = Message.OKIRF();
								buffOut.write(confirm.getData());
								buffOut.flush();
							}else{
								Message confirm = Message.NOKRF();
								buffOut.write(confirm.getData());
								buffOut.flush();
							}
						}
						
					} catch (ParseException e1) {
					} catch (NumberOfBytesException e1) {
					} catch (UnknownTypeMesssage e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				if (requete.equals("liste")) {

					try {
						
						Message msg = Message.LIST_INTERRO();
						
						buffOut.write(msg.getData());
						buffOut.flush();
						
						
						byte[] tmpMsg2 = new byte[Message.maxSIZEmsg];
						int sizeReturn = buffIn.read(tmpMsg2);
						Message msgRCV = Message.parseMessageWithSize(tmpMsg2, sizeReturn);
						System.out.println("REPONSE RECU : " + msgRCV.toString());
						
						if(!(msgRCV.getType()==TypeMessage.RLIST)){
							System.out.println("LE SERVEUR EST NULL");	
						}else{
							int nbItem=msgRCV.getNumItem();
							
							System.out.println("size liste  "+nbItem);
							for(int i=0;i<nbItem;i++){
								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
								}
								sizeReturn = buffIn.read(tmpMsg2);
								msgRCV = Message.parseMessageWithSize(tmpMsg2, sizeReturn);
								System.out.println("REPONSE RECU : " + msgRCV.toString());
								

							}
							
							System.out.println("FINI de recevoir des item du serveur");
						}
					

					} catch (Exception e1) {
						e1.printStackTrace();
					}

				}

			}
		}

	}

	public static void main(String[] args) throws Exception {
		System.out.println("CHOISSISZE PORT UDP");

		sc2 = new Scanner(System.in);

		int portUdp = sc2.nextInt();
		
		
		Client leClient =new Client("127.0.0.1", 9999, portUdp);
		Runnable tcp= new Runnable() {
			
			
			public void run() {
				try {
					leClient.tcpCommunication();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		Runnable udp= new Runnable() {
			
			
			public void run() {
				try {
					leClient.udpCommunication();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		leClient.threadUDP=new Thread(udp);
		leClient.threadUDP.start();
		leClient.threadTCP=new Thread(tcp);
		leClient.threadTCP.start();
		
		
		
		
	}
}
