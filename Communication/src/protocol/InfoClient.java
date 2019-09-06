package protocol;

import java.util.ArrayList;
import java.util.LinkedList;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class InfoClient {

	public boolean unknow;
	
	public boolean errorHappend;
	public int num_messWAITING;
	public InfoClient num_messID_ENCOURT;
	public boolean num_Mess_ENCOUR;
	
	
	
	public String id;
	private String IPAddressUDP;
	private int UDPport;
	private int mdp;
	private boolean connect;

	public SocketChannel actualSocketTCP;
	
	public SelectionKey keyIntheSelect;
	
	public ArrayList<InfoClient> friend;
	public ArrayList<InfoClient> friendRequest;
	public LinkedList<Message> fluxTosend;
	public LinkedList<Message> fluxSended;
	
	public boolean isSame(String id , int mdp){
		
		return this.id.equals(id) && this.mdp == mdp ; 
		
	}

	public InfoClient(SocketChannel actualSocketTCP){
		this.unknow=true;
		this.actualSocketTCP=actualSocketTCP;
		
		this.friendRequest = new ArrayList<InfoClient> ();
		
		this.friend = new ArrayList<InfoClient> ();
		this.fluxTosend = new LinkedList<Message> ();
		this.fluxSended = new LinkedList<Message> ();
		
	}

	public int getUDPPort () {
		return this.UDPport;
	}

	public String getIPAddressUDP () {
		return this.IPAddressUDP;
	}

	public boolean isConnected () {
		return this.connect;
	}

	public void setUDPPort (int port) {
		this.UDPport = port;
	}

	public void setIPAddress (String address) {
		this.IPAddressUDP = address;
	}

	public void setConnected (boolean value) {
		this.connect = value;
	}
	
	public String getIdClient(){
		return id;
	}

	public void toPrint() {
		
		for (int i = 0; i < this.friend.size (); i++) {
			
		}
	}

	public int getMdp() {
		return mdp;
	}

	public void setMdp(int mdp) {
		this.mdp = mdp;
	}

}
