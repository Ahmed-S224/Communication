package protocol;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;

public class Serveur {
	
	public Thread threadClient;
	public Thread threadPromoteur;

	private ServerSocketChannel servClient;
	private ServerSocket servPromoteur;
	private DatagramSocket servUDP;
	private Selector select;

	Object registeringSync = new Object(); // mutex contre deadlock avec wakeup

	private InfoClient[] arrayOfClient;

	public Serveur() throws IOException {

		this.servPromoteur = new ServerSocket(9998);
		this.servUDP = new DatagramSocket();

		this.arrayOfClient = new InfoClient[100];
		this.select = Selector.open();

		this.servClient = ServerSocketChannel.open();

		this.servClient.configureBlocking(false);

		SocketAddress port = new InetSocketAddress(9999);
		this.servClient.bind(port);

		servClient.register(select, SelectionKey.OP_ACCEPT);

		updateSelect();

	}
	
	InfoClient findClientInArray( SocketChannel socketActuelClient, boolean UnknowNotAccepted){
		InfoClient result=null;
		for (int i = 0; i < 100; i++) {
			result = this.arrayOfClient[i];
			if (result != null) {
				
				if(UnknowNotAccepted){
					if(result.unknow){
						continue;
					}
				}
				if (result.actualSocketTCP.socket().getInetAddress() == socketActuelClient.socket()
						.getInetAddress()
						&& result.actualSocketTCP.socket().getPort() == socketActuelClient.socket().getPort()) {
					return result;
				}
			}
		}
		
		return null;
	}
	
	
	private void addToSelect(InfoClient clientToAdd) {

		try {
			clientToAdd.keyIntheSelect = clientToAdd.actualSocketTCP.register(select, SelectionKey.OP_READ);
		} catch (ClosedChannelException e) {
			e.printStackTrace();
		}

		synchronized (registeringSync) {
			this.select.wakeup();
		}
	}

	private void updateSelect() {

		InfoClient actuel;

		for (int i = 0; i < 100; i++) {
			if (this.arrayOfClient[i] != null) {

				actuel = this.arrayOfClient[i];

				if (!actuel.isConnected()) {
					actuel.keyIntheSelect.cancel();
				}
			}
		}

		synchronized (registeringSync) {
			this.select.wakeup();
		}

	}

	public int findFirstEmptyPosition() {
		int result = 0;
		for (int i = 0; i < 100; i++) {
			if (this.arrayOfClient[i] == null) {
				result = i;
				break;
			}

		}
		return result;
	}

	public void funIQUIT(Message msgReceve,InfoClient actualClient) throws IOException {
	
		/*
		for (int i = 0; i < 100; i++) {
			InfoClient actualClient = this.arrayOfClient[i];

			if (actualClient != null) {
				
				if(actualClient.unknow){
					continue;
				}

				if (actualClient.actualSocketTCP.socket().getInetAddress() == socketActuelClient.socket()
						.getInetAddress()
						&& actualClient.actualSocketTCP.socket().getPort() == socketActuelClient.socket().getPort()) {
					
				}
			}
		}
		*/
		
		System.out.println(actualClient.getIdClient() + "se deconnecte");
		actualClient.setConnected(false);

		updateSelect();

		Message msg = Message.GOBYE();
		ByteBuffer buffout = ByteBuffer.wrap(msg.getData());
		actualClient.actualSocketTCP.write(buffout);
		//socketActuelClient.write(buffout);

	}

	public void funFRIE(Message msgReceve, InfoClient actualClient) throws IOException {
		boolean res = true;
		
		for (int i = 0; i < 100; i++) {
			InfoClient amis = null;
			if (arrayOfClient[i] != null && arrayOfClient[i].getIdClient().equals(msgReceve.getId())) {
				
				amis=arrayOfClient[i];
				
				actualClient.friendRequest.add(amis);
				amis.friendRequest.add(actualClient);
				
				System.out.println(amis.id + " JE T'AI TROUVE !!!!");
				res = false;
				Message msg = Message.FRIE_POSITIVE();

				ByteBuffer buffout = ByteBuffer.wrap(msg.getData());
				actualClient.actualSocketTCP.write(buffout);
				
				try{
					byte valY =0;
					Message ftrMsg = Message.EIRF_POSITIVE(actualClient.id);
					amis.fluxTosend.add(ftrMsg);
					
					Message YXX_MSG = Message.XYY(valY, (char) amis.fluxTosend.size());
					DatagramPacket p= new DatagramPacket(YXX_MSG.getData(),YXX_MSG.getData().length,InetAddress.getByName(amis.getIPAddressUDP()),amis.getUDPPort());
					
					this.servUDP.send(p);
					
					} catch(Exception e){
					e.printStackTrace();
				}

			}

		}
		if (res) {
			Message msg = Message.FRIE_NEGATIVE();
			ByteBuffer buffout = ByteBuffer.wrap(msg.getData());
			actualClient.actualSocketTCP.write(buffout);

		}
	}

	public void funLIST_INTERRO(Message msgReceve, InfoClient actualClient) {

		LinkedList<InfoClient> listClientTOsend = new LinkedList<>();
		
		
		for (int i = 0; i < 100; i++) {
			if (this.arrayOfClient[i] != null) {
				
				if (this.arrayOfClient[i].unknow) {
					continue;
				}
				
				listClientTOsend.add(this.arrayOfClient[i]);
				//System.out.println("CLIENT ISNCRIT DANS SERV "+this.arrayOfClient[i].getIdClient());
				
			}

		}
		
		try {
			Message msg = Message.RLIST(listClientTOsend.size());
			ByteBuffer buffout = ByteBuffer.wrap(msg.getData());
			actualClient.actualSocketTCP.write(buffout);
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
			
			/*for(InfoClient clientTosend : listClientTOsend){
				msg = Message.LINUM(clientTosend.id);
				buffout = ByteBuffer.wrap(msg.getData());
				System.out.println("ESSAY de MESS "+msg.toString());
				actualClient.actualSocketTCP.write(buffout);
				
			}*/
			int Retry=10;
			int sizeSend=0;
			for(InfoClient clientTosend : listClientTOsend){

				msg = Message.LINUM(clientTosend.id);
				buffout = ByteBuffer.wrap(msg.getData());
				
				while(Retry>0 && (sizeSend==0 || sizeSend==-1)){
					
					System.out.println("ESSAY de MESS "+msg.toString());
					sizeSend=actualClient.actualSocketTCP.write(buffout);
					
					if(sizeSend==0 || sizeSend==-1){
						System.out.println("TAILLE MESS ENVOYER (PROBLEM) "+sizeSend);
					}
					Retry--;
					
				}
				Retry=10;
				sizeSend=0;
				System.out.println("SERV , MESS envoyé -> "+msg.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void funMENUM(Message msgReceve, InfoClient actualClient) {

		

		
				try {
					
					if (!actualClient.num_Mess_ENCOUR) {
						System.out.println("SERV " + actualClient.id + " n'est pas dans une procedure de message");
						
						Message msg = Message.MESS_NEGATIVE();
						ByteBuffer buffout = ByteBuffer.wrap(msg.getData());
						actualClient.actualSocketTCP.write(buffout);
						

					}else{ 
					InfoClient ami = actualClient.num_messID_ENCOURT;

					Message msgFlux = Message.MUNEM(msgReceve.getNum(), msgReceve.getMsg());

					ami.fluxTosend.add(msgFlux);

					if (actualClient.num_messWAITING == 1) {
						
						if(!actualClient.errorHappend){
							Message msg = Message.MESS_NEGATIVE();
							ByteBuffer buffout = ByteBuffer.wrap(msg.getData());
							actualClient.actualSocketTCP.write(buffout);
						}else{
							byte valY = 3;
							Message YXX_MSG = Message.XYY(valY, (char) ami.fluxTosend.size());
							DatagramPacket p = new DatagramPacket(YXX_MSG.getData(), YXX_MSG.getData().length,
									InetAddress.getByName(ami.getIPAddressUDP()), ami.getUDPPort());
							this.servUDP.send(p);
							Message msg = Message.MESS_POSITIVE();
							ByteBuffer buffout = ByteBuffer.wrap(msg.getData());
							actualClient.actualSocketTCP.write(buffout);
						}
						
						actualClient.num_Mess_ENCOUR=false;
						actualClient.num_messID_ENCOURT=null;
						actualClient.errorHappend=false;
					

					}
					}

					actualClient.num_messWAITING--;
				} catch (Exception e) {

				}

		

	}
	
	public void funMESS_INTERRO(Message msgReceve, InfoClient actualClient) throws IOException {


		if(actualClient.num_Mess_ENCOUR){
			System.out.println("SERV " + actualClient.id + " est deja dans une procedure de message");
		}else{
			if (actualClient.friend == null) {
				System.out.println("SERV " + actualClient.id + " n'a pas d'amis");
			}else{
				String idAmi = msgReceve.getId();
				for (InfoClient ami : actualClient.friend) {
					if (ami.getIdClient().equals(idAmi)) {
						
						actualClient.num_messID_ENCOURT=ami;
						actualClient.num_messWAITING = msgReceve.getNum_mess();
						actualClient.num_Mess_ENCOUR=true;
						actualClient.errorHappend=true;
						break;
					}
				}
			}

		}
		
		
	}
		
	
	
	
	public void funFREI(InfoClient actualClient, boolean confirmer,Message lastSend) {

		
		System.out.println("funFREI entry");
		String IdAmiValider = lastSend.getId();

		for (InfoClient ami : actualClient.friendRequest) {

			try {

				if (ami.id.equals(IdAmiValider)) {

					System.out.println("funFREI -> ami find");
					actualClient.friendRequest.remove(ami);
					ami.friendRequest.remove(actualClient);

					Message ftrMsg;
					byte valY;

					if (confirmer) {

						actualClient.friend.add(ami);
						ami.friend.add(actualClient);
						ftrMsg = Message.FRIEN(actualClient.id);
						valY = 1;

					} else {
						ftrMsg = Message.NOFRI(actualClient.id);
						valY = 2;
					}

					ami.fluxTosend.add(ftrMsg);

					Message YXX_MSG = Message.XYY(valY, (char) ami.fluxTosend.size());
					DatagramPacket p = new DatagramPacket(YXX_MSG.getData(), YXX_MSG.getData().length,
							InetAddress.getByName(ami.getIPAddressUDP()), ami.getUDPPort());

					this.servUDP.send(p);

					break;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
	
	
	public void funOKFR_NOKFR(InfoClient actualClient, boolean confirmer) {
		System.out.println("fun entry");
		if(actualClient.fluxSended.isEmpty()){
			System.out.println("fun , flux vide");
			return;
		}
		Message lastSend = actualClient.fluxSended.pop();
		
		System.out.println("fun , message type -> "+lastSend.toString());
		
		if (lastSend.getType() == TypeMessage.EIRFsup) {
			funFREI(actualClient,confirmer,lastSend);
		}
		

	}
	
	public void funCONSU(InfoClient actualClient) throws IOException{
		Message msgToSend;
		if(actualClient.fluxTosend.isEmpty()){
			msgToSend=Message.NOCON();
		}else{
			msgToSend=actualClient.fluxTosend.pop();	
		}

		actualClient.fluxSended.add(msgToSend);
		ByteBuffer buffout = ByteBuffer.wrap(msgToSend.getData());
		actualClient.actualSocketTCP.write(buffout);
		
	}
	
	public void funFLOO_INTERRO(Message msgReceve, InfoClient actualClient) throws IOException{
		
		if(actualClient.friend==null){
			System.out.println("Vous n'avez pas d'amis");
			return;
		}
		try {
			Message msgInondation = Message.OOLF_POSITIVE(actualClient.id, msgReceve.getMsg());
			
			byte valY =4;
			for (InfoClient ami : actualClient.friend) {

				ami.fluxTosend.add(msgInondation);
				
				Message YXX_MSG = Message.XYY(valY, (char) ami.fluxTosend.size());	
				DatagramPacket p = new DatagramPacket(YXX_MSG.getData(), YXX_MSG.getData().length,
						InetAddress.getByName(ami.getIPAddressUDP()), ami.getUDPPort());
				this.servUDP.send(p);
				
			}
		} catch (Exception e) {
			System.out.println("ATTENTION MSGPARSE DANS MESSi");
		}
	}

	public void funCONNE(Message msgReceve,SocketChannel actualSocket, InfoClient actualClient) throws IOException {
		
		//InfoClient reelClient=null;
		InfoClient tmp;
		
		for (int i = 0; i < 100; i++) {
			tmp = this.arrayOfClient[i];
			System.out.println("SOCKET ANCIEN -> "+tmp.actualSocketTCP);
			System.out.println("SOCKET NOUVEAU -> "+actualSocket);
			if (tmp != null) {

				if (tmp.id.equals(msgReceve.getId())) {
					actualClient.friend=tmp.friend;
					actualClient.unknow=tmp.unknow;
					actualClient.friendRequest=tmp.friendRequest;
					actualClient.fluxSended=tmp.fluxSended;
					actualClient.fluxTosend=tmp.fluxTosend;
					actualClient.id=tmp.id;
					actualClient.setMdp(tmp.getMdp());
					actualClient.setUDPPort(tmp.getUDPPort());
					this.arrayOfClient[i] = null;
					//updateSelect();
					break;
				}
			}

		}
		
		if(actualClient!=null){
			if(actualClient.isSame(msgReceve.getId(), msgReceve.getMdp())){
				System.out.println(actualClient.getIdClient() + " JE T'AI TROUVE !!!!");
				
				
			
				actualClient.setConnected(true);
				//updateSelect();

				Message msg = Message.HELLO();
				ByteBuffer buffout = ByteBuffer.wrap(msg.getData());
				actualSocket.write(buffout);
			}
		}
		
	
		Message msg = Message.GOBYE();
		ByteBuffer buffout = ByteBuffer.wrap(msg.getData());
		actualClient.actualSocketTCP.write(buffout);
		
	
	}

	public void funREGIS(Message msgReceve, SocketChannel socketActuelClient,InfoClient actualClient) throws IOException {

		System.out.println("IP DU CLIENT : " + socketActuelClient.socket().getInetAddress().getHostAddress());

		
		if(!actualClient.unknow){
			Message msg = Message.GOBYE();
			ByteBuffer buffout = ByteBuffer.wrap(msg.getData());
			actualClient.actualSocketTCP.write(buffout);
		}else{
			actualClient.unknow=false;
			actualClient.id=msgReceve.getId();
			actualClient.setIPAddress(socketActuelClient.socket().getInetAddress().getHostAddress());
			actualClient.setUDPPort(msgReceve.getPort());
			actualClient.setMdp( msgReceve.getMdp());
			System.out.println("AJOUT : " + " unknow client devient "+actualClient.getIdClient());
			Message msg = Message.WELCO();
			ByteBuffer buffout = ByteBuffer.wrap(msg.getData());
			actualClient.actualSocketTCP.write(buffout);
		}
		
	}

	public void listenClient() throws IOException {

		SocketChannel socketActuelClient;

		System.out.println("START SERV -> LISTEN TCP CLIENT");

		int valReturnOfSelect;

		Iterator<SelectionKey> it;

		while (true) {

			System.out.println("ON RENTRE DANS LE SELECT");
			valReturnOfSelect = this.select.select();
			System.out.println("ON SORT DU SELECT");
			if (valReturnOfSelect == 9) {
				// TODO
				// break;
			}
			synchronized (registeringSync) {
				//Set<SelectionKey> set = this.select.selectedKeys();

				// System.out.println("NOMBRE D EVENT : "+set.size());

				it = this.select.selectedKeys().iterator();

				while (it.hasNext()) {
					// System.out.println("IL YA ENCORE UN EVENT A GERER");
					SelectionKey sk = it.next();
					it.remove();

					if (sk.isAcceptable() && sk.channel() == this.servClient) {

						socketActuelClient = this.servClient.accept();
						socketActuelClient.configureBlocking(false);

						System.out.println("J'ai recu une nouvelle connection d'un client");

						int posi = findFirstEmptyPosition();
						InfoClient clientUnknow = new InfoClient(socketActuelClient);
						this.arrayOfClient[posi] = clientUnknow;
						addToSelect(clientUnknow);

					} else {
						
						InfoClient actualClient;

						for (int i = 0; i < 100; i++) {

							if (this.arrayOfClient[i] == null) {
								continue;
							}

							
							actualClient= this.arrayOfClient[i];
							socketActuelClient =actualClient.actualSocketTCP;
							
							
							
							if(!actualClient.keyIntheSelect.isValid()){
								System.out.println("LA CLEF DU CLIENT NEST PLUS VALIDE");
								
								actualClient.actualSocketTCP.close();
								continue;
								
							}

							if ( sk.isReadable() && sk.channel() == socketActuelClient) {


								byte[] tmpMsg2 = new byte[Message.maxSIZEmsg];

								ByteBuffer buff = ByteBuffer.wrap(tmpMsg2);

								int sizeReturn = socketActuelClient.read(buff);

								tmpMsg2 = buff.array();

								System.out.println("JAI RECU UN MESSAGE");
								if (sizeReturn <= 0) {
									System.out.println("SOCKET FERMER");
									break;
								} else {
									try {

										Message msgReceve = Message.parseMessageWithSize(tmpMsg2, sizeReturn);

										System.out.println("MSG RECEVE :" + msgReceve.toString());

										if (msgReceve.getType() == TypeMessage.REGIS) {
											funREGIS(msgReceve, socketActuelClient,actualClient);
										} else if (msgReceve.getType() == TypeMessage.FRIEi) {
											funFRIE(msgReceve,actualClient);
										} else if (msgReceve.getType() == TypeMessage.CONNE) {
											funCONNE(msgReceve, socketActuelClient, actualClient);
										} else if (msgReceve.getType() == TypeMessage.IQUIT) {
											funIQUIT(msgReceve,actualClient);
										}else if(msgReceve.getType() == TypeMessage.MESSi){
											funMESS_INTERRO(msgReceve, actualClient);
										}else if(msgReceve.getType() == TypeMessage.MENUM){
											funMENUM(msgReceve, actualClient);
										}else if (msgReceve.getType() == TypeMessage.CONSU){
											funCONSU(actualClient);
										}else if(msgReceve.getType() == TypeMessage.FLOOi){
											funFLOO_INTERRO(msgReceve, actualClient);
										}else if (msgReceve.getType() == TypeMessage.OKIRF){
											funOKFR_NOKFR(actualClient,true);
										}else if(msgReceve.getType() == TypeMessage.NOKRF){
											funOKFR_NOKFR(actualClient,false);
										}else if(msgReceve.getType() == TypeMessage.LISTi){
											funLIST_INTERRO(msgReceve,actualClient);
										}

									} catch (Exception e) {
										e.printStackTrace();
										System.out.println("ECHEC SERV");
									}

								}

							}
						}

					}
				}
			}
		}


	}

	public void listenPromoteur() throws IOException {

		Socket tmp;
		BufferedOutputStream buffOut;
		BufferedInputStream buffIn;

		System.out.println("START SERV -> LISTEN TCP PROMOTEUR");
		while (true) {

			tmp = this.servPromoteur.accept();

			buffOut = new BufferedOutputStream(tmp.getOutputStream());
			buffIn = new BufferedInputStream(tmp.getInputStream());

			byte[] tmpMsg2 = new byte[Message.maxSIZEmsg];

			int sizeReturn = buffIn.read(tmpMsg2);

			if (sizeReturn <= 0) {
				System.out.println("SOCKET FERMER");
				break;
			} else {
				try {

					// System.out.println("taille recu "+sizeReturn);

					Message msgReceve = Message.parseMessageWithSize(tmpMsg2, sizeReturn);

					
					System.out.println("MSG RECEVE :" + msgReceve.toString());
					
					if(! (msgReceve.getType()==TypeMessage.PUBLi) ){
						continue;
					}
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
					}
					InfoClient actualClient;
					byte valY = 5;
					for(int i =0; i<100; i++){
						actualClient = this.arrayOfClient[i];
						
						if(actualClient != null){
							
							Message promoReceve = Message.LBUP_POSITIVE(msgReceve.getIp_diff(), msgReceve.getPort(), msgReceve.getMsg());

							Message msg = Message.PUBL_POSITIVE();

							buffOut.write(msg.getData());
							buffOut.flush();

							try {
								actualClient.fluxTosend.add(promoReceve);
								Message YXX_MSG = Message.XYY(valY, (char) actualClient.fluxTosend.size());
								DatagramPacket p = new DatagramPacket(YXX_MSG.getData(), YXX_MSG.getData().length,
										InetAddress.getByName(actualClient.getIPAddressUDP()),
										actualClient.getUDPPort());
								this.servUDP.send(p);
							} catch (Exception e) {

							}
						
						}
					}
					

				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("ECHEC SERV");
				}
			}

		}

		tmp.close();
	}

	public static void main(String[] args) throws IOException {

		Serveur t = new Serveur();
		
		Runnable promoteur= new Runnable() {
			
			
			public void run() {
				try {
					t.listenClient();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		Runnable client= new Runnable() {
			
			
			public void run() {
				try {
					t.listenPromoteur();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		t.threadClient=new Thread(client);
		t.threadClient.start();
		t.threadPromoteur=new Thread(promoteur);
		t.threadPromoteur.start();

	}

}
