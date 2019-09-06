package protocol;
import protocol.exceptions.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class Message {
	
	private boolean multi;

	private byte[] data;
	private int sizeMsg;
	
	private TypeMessage type;
	
	private byte Y;
	private char XX;
	private byte[] XXLITTLE_ENDIAN_2;
	
	private String id;
	private String mess;
	private String ip_diff;
	private String prom_mess;
	private String ip_succ;
	
	private Integer num;
	private String numString;
	
	private Integer num_mess;
	private String num_messString;
	
	private Integer num_item;
	private String num_itemString;
	
	private char mdp;
	private byte[] mdpLITTLE_ENDIAN_2;
	
	private Integer port;
	private String portString;
	
	private String id_app;
	private byte[] data_app;
	
	public final static int maxSIZEmsg = 512;
	public final static int maxMsg = 200;
	public final static int maxPromMsg = 300;
	public final static int byteSizeType = 5;
	public final static int byteSizeProm = 4;
	public final static int byteSizeId = 8;
	public final static int byteSizeIP = 15;
	public final static int byteSizePort = 4;
	public final static int byteSizeTypeMSG = 4;
	public final static int byteSizeSpace = 1;
	public final static int byteSizeMdp = 2;
	public final static int byteSizeItem = 3;
	public final static int numOfPlus=3;
	
public final static ByteOrder byteORDER_MDP=ByteOrder.LITTLE_ENDIAN;
	
	/**
	 * Create a new Message and Parse it from data
	 * @param data le contenu du message a parser
	 * @throws UnknownTypeMesssage if the type Message is unknow
	 * @throws ParseException if the data do no correcpond to the Type Message
	 * @throws NumberOfBytesException 
	 * @throws IpException 
	 */
	public static Message parseMessage(byte [] data) throws ParseException, UnknownTypeMesssage, NumberOfBytesException {
		return new Message(data);
	}
	
	public static Message parseMessageWithSize(byte [] data,int sizeMsg) throws ParseException, UnknownTypeMesssage, NumberOfBytesException {
		return new Message(data,sizeMsg);
	}
	
	
	private Message(byte[] data,int sizeMsg) throws ParseException, UnknownTypeMesssage, NumberOfBytesException {
		super();
		this.sizeMsg = sizeMsg;
		this.data = data;
		try {
			this.parse();
		} catch (IndexOutOfBoundsException e) {
			throw new ParseException();
		}
		this.convertALL();
		
		
	}
	
	private Message(byte[] data) throws ParseException, UnknownTypeMesssage, NumberOfBytesException {
		super();
		this.data = data;
		try {
			this.parse();
		} catch (IndexOutOfBoundsException e) {
			throw new ParseException();
		}
		this.convertALL();
	}
	
	/**
	 * Constructeur pour les Appels statique , pattern FACTORIE
	 * @param data
	 * @param type
	 */
	private Message(byte[] data, TypeMessage type) {
		super();
		this.setMulti(false);
		this.data = data;
		this.type = type;
	}
	/**
	 * Convertir les chiffres dans la representation attendu par RINGO
	 * @param msg
	 * @throws ParseException 
	 * @throws NumberOfBytesException 
	 * @throws IpException 
	 */
	private void convertALL() throws ParseException, NumberOfBytesException{
		if(this.port!=null){
			this.portString=convertPort(this.port);
		}
		if(this.num_item!=null){
			this.num_itemString=convertItem(this.num_item);
		}
		if(this.num_mess!=null){
			this.num_messString=convertPort(this.num_mess);
		}
		if(this.num!=null){
			this.numString=convertPort(this.num);
		}
		
		this.mdpLITTLE_ENDIAN_2=Message.charToByteArray(this.mdp,byteSizeMdp,byteORDER_MDP);
		this.XXLITTLE_ENDIAN_2=Message.charToByteArray(this.XX,byteSizeMdp,byteORDER_MDP);

		
	}
	
	/**
	 * Retourne This.data entre N et SIZE-N
	 * 
	 * @param n debut 
	 * @param size taille demander
	 * @return le string de this.data de N jusqu'a Size-N
	 */
	private String getDataFrom_N(int n, int size) {
		try{
			String tmp = new String(this.data, n, size);
			return tmp;
		}catch(StringIndexOutOfBoundsException e){
			return "";
		}
	}
	
	
	private byte[] getDataFrom_N_byte(int n, int size) {
		try{
			byte [] tmp =new byte [size];
			for(int i=0; i<size ; i++){
				tmp[i]=this.data[n+i];
			}
			return tmp;
		}catch(StringIndexOutOfBoundsException e){
			return null;
		}
	}
	
	/**
	 * Parcer le contenu d'un nouveau message
	 * 
	 * @throws UnknownTypeMesssage
	 * @throws IndexOutOfBoundsException
	 * @throws ParseException
	 */
	private void parse() throws IndexOutOfBoundsException,UnknownTypeMesssage, ParseException{
		int curseur=0;
		String strParsed;
		
		if(this.sizeMsg==3){
			
			this.type = TypeMessage.YXX;
			this.Y=getDataFrom_N_byte(curseur, 1)[0];
			curseur++;
			byte arr [] =getDataFrom_N_byte(curseur, 2);
			this.XX= byteArrayToChar(arr, 2, byteORDER_MDP);
			return;
			
		}
		
		strParsed=getDataFrom_N(curseur,byteSizeProm);

		try {

			this.type = TypeMessage.valueOf(strParsed);
			
			//PROM
			curseur+=byteSizeProm;
			parseTestSpace(curseur);
			curseur++;
			
			int sizeMess = this.sizeMsg - byteSizeProm - 1;
			
			System.out.println("size mess inside : "+sizeMess);
			
			strParsed=getDataFrom_N(curseur, sizeMess);

			this.mess=strParsed;
			
			curseur+=sizeMess;
			
			return;
			
		} catch (IllegalArgumentException e) {
			//TODO
		}
		
		
		strParsed=getDataFrom_N(curseur,byteSizeType);
		
		curseur+=byteSizeType;
		//System.out.println("type reconnu : "+strParsed);
		
		
		if(strParsed.charAt(4) == '?'){
			strParsed = strParsed.substring(0, byteSizeType-1) + "i";
		}

		if(strParsed.charAt(4) == '<'){
			strParsed = strParsed.substring(0, byteSizeType-1) + "inf";
		}

		if(strParsed.charAt(4) == '>'){
			strParsed = strParsed.substring(0, byteSizeType-1) + "sup";
		}
				
		try{
			
			//System.out.println("TYPE DETECT : "+strParsed);
			this.type=TypeMessage.valueOf(strParsed);
		}catch(IllegalArgumentException e){
			throw new UnknownTypeMesssage();
		}
		
		
		if(type==TypeMessage.WELCO || type==TypeMessage.GOBYE || type==TypeMessage.HELLO 
				|| type==TypeMessage.FRIEsup || type==TypeMessage.FRIEinf || type==TypeMessage.MESSsup
				|| type==TypeMessage.MESSinf || type==TypeMessage.FLOOsup || type==TypeMessage.LISTi
				|| type==TypeMessage.CONSU || type==TypeMessage.OKIRF || type==TypeMessage.NOKRF
				|| type==TypeMessage.NOCON || type==TypeMessage.IQUIT || type==TypeMessage.PUBLsup){
			
			strParsed=getDataFrom_N(curseur,numOfPlus);
			parseTroisPlus(strParsed);
			curseur++;
			//parseTestEnd(curseur+1);
			return;
		}
		
		parseTestSpace(curseur);
		curseur++;
		
		if(type==TypeMessage.FRIEi || type==TypeMessage.LINUM || type==TypeMessage.EIRFsup
				|| type==TypeMessage.FRIEN || type==TypeMessage.NOFRI){
			
				strParsed=getDataFrom_N(curseur, byteSizeId);
				this.id=strParsed;
				curseur+=byteSizeId;
				strParsed=getDataFrom_N(curseur,numOfPlus);
				parseTroisPlus(strParsed);
				return;
		}
		
		if(type==TypeMessage.REGIS){
			strParsed=getDataFrom_N(curseur, byteSizeId);
			this.id=strParsed;
			curseur+=byteSizeId;
			parseTestSpace(curseur);
			curseur++;
			strParsed=getDataFrom_N(curseur, byteSizePort);
			parseTestPort(strParsed);
			this.port=Integer.parseInt(strParsed);
			curseur+=byteSizePort;
			parseTestSpace(curseur);
			curseur++;
			byte [] valMdp =getDataFrom_N_byte(curseur, byteSizeMdp);
			this.mdp = byteArrayToChar(valMdp, byteSizeMdp, byteORDER_MDP);
			curseur+=byteSizeMdp;
		
			strParsed=getDataFrom_N(curseur,numOfPlus);
			parseTroisPlus(strParsed);
			return;
			
		}
		
		if(type==TypeMessage.CONNE){
			strParsed=getDataFrom_N(curseur, byteSizeId);
			this.id=strParsed;
			curseur+=byteSizeId;
			parseTestSpace(curseur);
			curseur++;
			byte [] valMdp =getDataFrom_N_byte(curseur, byteSizeMdp);
			this.mdp = byteArrayToChar(valMdp, byteSizeMdp, byteORDER_MDP);
			curseur+=byteSizeMdp;
			strParsed=getDataFrom_N(curseur,numOfPlus);
			parseTroisPlus(strParsed);
			return;
			
		}
		
		if(type==TypeMessage.MESSi || type==TypeMessage.SSEMsup){
			strParsed=getDataFrom_N(curseur, byteSizeId);
			this.id=strParsed;
			curseur+=byteSizeId;
			parseTestSpace(curseur);
			curseur++;
			strParsed = getDataFrom_N(curseur, byteSizePort);
			this.num_mess=Integer.parseInt(strParsed);
			curseur+=byteSizePort;
			strParsed=getDataFrom_N(curseur,numOfPlus);
			parseTroisPlus(strParsed);
			return;
			
		}
		
		if(type==TypeMessage.MENUM || type==TypeMessage.MUNEM){
			strParsed = getDataFrom_N(curseur, byteSizePort);
			this.num=Integer.parseInt(strParsed);
			curseur+=byteSizePort;
			parseTestSpace(curseur);
			curseur++;
			int sizeMess = this.sizeMsg - byteSizeType - 1 - byteSizePort -1 - numOfPlus;
			strParsed=getDataFrom_N(curseur, sizeMess);
			this.mess=strParsed;
			curseur+=sizeMess;			
			strParsed=getDataFrom_N(curseur,numOfPlus);
			parseTroisPlus(strParsed);
			return;
			
			
		}
		
		if(type==TypeMessage.FLOOi){
			int sizeMess = this.sizeMsg -byteSizeType - 1 - numOfPlus;
			strParsed=getDataFrom_N(curseur, sizeMess);
			this.mess=strParsed;
			curseur+=sizeMess;			
			strParsed=getDataFrom_N(curseur,numOfPlus);
			parseTroisPlus(strParsed);
			return;
		}
		
		if(type==TypeMessage.RLIST){
			strParsed = getDataFrom_N(curseur, byteSizeItem);
			this.num_item=Integer.parseInt(strParsed);
			curseur+=byteSizeItem;
			strParsed=getDataFrom_N(curseur,numOfPlus);
			parseTroisPlus(strParsed);
			return;
		}
		
		if(type==TypeMessage.OOLFsup){
			strParsed=getDataFrom_N(curseur, byteSizeId);
			this.id=strParsed;
			curseur+=byteSizeId;
			parseTestSpace(curseur);
			curseur++;
			int sizeMess = this.sizeMsg - byteSizeType - 1 - byteSizeId -1 - numOfPlus;
			strParsed=getDataFrom_N(curseur, sizeMess);
			this.mess=strParsed;
			curseur+=sizeMess;			
			strParsed=getDataFrom_N(curseur,numOfPlus);
			parseTroisPlus(strParsed);;
			return;
		}
		
		if(type==TypeMessage.LBUPsup || type==TypeMessage.PUBLi){
			strParsed=getDataFrom_N(curseur, byteSizeIP);
			this.ip_diff=strParsed;
			curseur+=byteSizeIP;
			parseTestSpace(curseur);
			curseur++;
			strParsed=getDataFrom_N(curseur, byteSizePort);
			parseTestPort(strParsed);
			this.port=Integer.parseInt(strParsed);
			curseur+=byteSizePort;
			parseTestSpace(curseur);
			curseur++;
			int sizeMess = this.sizeMsg - byteSizeType - 1 - byteSizeIP -1 - byteSizePort -1 - numOfPlus;
			strParsed=getDataFrom_N(curseur, sizeMess);
			this.mess=strParsed;
			curseur+=sizeMess;			
			strParsed=getDataFrom_N(curseur,numOfPlus);
			parseTroisPlus(strParsed);

			return;
			
			
		}
		
		
	}
			
	/**
	 * Pour parse
	 * test si le caractere start est un caractere d'espace
	 * @param start
	 * @throws ParseException souleve une erreur si ce n'est pas un espace
	 */
	private void parseTestSpace(int start) throws ParseException{
		if(! (new String(this.data,start,1).equals(" "))){
			throw new ParseException();
		}
	}
	private void parseTroisPlus(String strParsed) throws ParseException{
		if(!strParsed.equals("+++")){
			throw new ParseException();
		}
	}
	
	/**
	 * Pour parse
	 * test si le parametre est un numero de port conventionel
	 * @param portTest
	 * @throws ParseException
	 */
	public static void parseTestPort(String portTest)throws ParseException{
		if(portTest.length()!=4){
			throw new ParseException();
		}
		try{
			int tmp=Integer.parseInt(portTest.substring(0,4));
			if(tmp<0 || tmp>9999){
				throw new ParseException();
			}
		}catch(NumberFormatException e){
			throw new ParseException();
		}
		
		
	}
	
	/**
	 * Afficher un message
	 */
	public String toString(){
		String str ="";
		
		if(type==TypeMessage.YXX){
			str+=(int)this.Y;
			str+=" ";
			/*
			for(int i =0;i<byteSizeMdp;i++){
				str+=(int)this.XXLITTLE_ENDIAN_2[i];
			}
			*/
			str+=(int)this.XX;
			return str;
		
		}
		
		str =this.type.toString();
		
		if(type==TypeMessage.WELCO || type==TypeMessage.GOBYE || type==TypeMessage.HELLO 
				|| type==TypeMessage.FRIEsup || type==TypeMessage.FRIEinf || type==TypeMessage.MESSsup
				|| type==TypeMessage.MESSinf || type==TypeMessage.FLOOsup || type==TypeMessage.LISTi
				|| type==TypeMessage.CONSU || type==TypeMessage.OKIRF || type==TypeMessage.NOKRF
				|| type==TypeMessage.NOCON || type==TypeMessage.IQUIT || type==TypeMessage.PUBLsup){
			
			return str+"+++";
		}
		
		if(type==TypeMessage.FRIEi || type==TypeMessage.LINUM || type==TypeMessage.EIRFsup
				|| type==TypeMessage.FRIEN || type==TypeMessage.NOFRI){
			
			return str+" "+this.id+"+++";
			
		}	
		
		
		if(type==TypeMessage.REGIS){
			str = str+" "+this.id+" "+this.portString;
			try {
				//str=str+" "+Message.byteArrayToChar(this.mdpLITTLE_ENDIAN_2, byteSizeMdp,byteORDER_MDP);
				str+=" ";
				for(int i =0;i<byteSizeMdp;i++){
					str+=(int)this.mdpLITTLE_ENDIAN_2[i];
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
			str = str+"+++";
			return str;
		}
		
		if(type==TypeMessage.CONNE){
			str = str+" "+this.id;
			try {
				//str=str+" "+Message.byteArrayToChar(this.mdpLITTLE_ENDIAN_2, byteSizeMdp,byteORDER_MDP);
				//str+=" "+(int)this.mdp;
				
				str+=" ";
				for(int i =0;i<byteSizeMdp;i++){
					str+=(int)this.mdpLITTLE_ENDIAN_2[i];
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
			str = str+"+++";
			return str;
		}
		
		if(type==TypeMessage.MESSi || type==TypeMessage.SSEMsup){
			return str+" "+this.id +" "+this.num_messString+"+++";
		}
		if(type==TypeMessage.MENUM || type==TypeMessage.MUNEM){
			return str+" "+this.numString+" "+this.mess+"+++";
		}
		
		if(type==TypeMessage.FLOOi){
			return str+" "+this.mess+"+++";
		}
		
		if(type==TypeMessage.RLIST){
			return str+" "+this.num_itemString+"+++";
		}
		
		if(type==TypeMessage.OOLFsup){
			return str+" "+this.id+" "+this.mess+"+++";
		}
		if(type==TypeMessage.LBUPsup || type==TypeMessage.PUBLi){
			return str+" "+this.ip_diff+" "+this.portString+" "+this.mess+"+++";
		}
		
		//TODO POURT TESTS
		else{
			return new String(this.data);
		}
	}
	
	public String toStringSHORT(int sizeMax){
		String tmp=this.toString();
		if(tmp.length()>sizeMax){
			return tmp.substring(0,sizeMax)+"...";
		}
		return tmp;
	}
	
	
	static Message REGIS(String id, int port, char mdp)throws ParseException, NumberOfBytesException {
		byte[] REGIS_array = new byte[5+1+byteSizeId+1+byteSizePort+1+byteSizeMdp+3];
		
		Message tmp=new Message(REGIS_array,TypeMessage.REGIS);
		tmp.id=id;
		tmp.mdp=mdp;
		tmp.port=port;
		tmp.convertALL();
		remplirData(REGIS_array,"REGIS ".getBytes(),tmp.id.getBytes(),
				(" "+tmp.portString+" ").getBytes(),tmp.mdpLITTLE_ENDIAN_2, "+++".getBytes());
		return tmp;
	}
	
	static Message WELCO() {
		byte[] WELCO_array = new String("WELCO+++").getBytes();
		Message tmp = new Message(WELCO_array, TypeMessage.WELCO);
		return tmp;
	}
	
	static Message HELLO() {
		byte[] HELLO_array = new String("HELLO+++").getBytes();
		Message tmp = new Message(HELLO_array, TypeMessage.HELLO);
		return tmp;
	}
	
	static Message GOBYE() {
		byte[] GOBYE_array = new String("GOBYE+++").getBytes();
		Message tmp = new Message(GOBYE_array, TypeMessage.GOBYE);
		return tmp;
	}
	
	static Message CONNE(String id, char mdp)throws ParseException, NumberOfBytesException {
		byte[] CONNE_array = new byte[5+1+byteSizeId+1+byteSizeMdp+3];
		
		Message tmp=new Message(CONNE_array,TypeMessage.CONNE);
		tmp.id=id;
		tmp.mdp=mdp;
		tmp.convertALL();
		remplirData(CONNE_array,"CONNE ".getBytes(),(tmp.id+" ").getBytes(),
				tmp.mdpLITTLE_ENDIAN_2, "+++".getBytes());
		return tmp;
	}
	
	static Message FRIE_INTERRO(String id) throws ParseException, NumberOfBytesException{
		byte[] FRIE_array = new byte[5+1+byteSizeId+3];

		Message tmp=new Message(FRIE_array,TypeMessage.FRIEi);

		tmp.id=id;
		if(id.length()!=8){
			throw new ParseException();
			
		}

		tmp.convertALL();

		remplirData(FRIE_array,"FRIE? ".getBytes(),tmp.id.getBytes(),"+++".getBytes());

		return tmp;
	}
	
	static Message FRIE_NEGATIVE() {
		byte[] FRIE_array = new String("FRIE<+++").getBytes();
		Message tmp = new Message(FRIE_array, TypeMessage.FRIEinf);
		return tmp;
	}
	
	static Message FRIE_POSITIVE() {
		byte[] FRIE_array = new String("FRIE>+++").getBytes();
		Message tmp = new Message(FRIE_array, TypeMessage.FRIEsup);
		return tmp;
	}
	
	static Message MESS_INTERRO(String id, int num_mess)throws ParseException, NumberOfBytesException {
		byte[] MESS_array = new byte[5+1+byteSizeId+1+byteSizePort+3];
		
		Message tmp=new Message(MESS_array,TypeMessage.MESSi);
		tmp.id=id;
		if(id.length()!=8){
			throw new ParseException();
		}
		tmp.num_mess=num_mess;
		tmp.convertALL();
		remplirData(MESS_array,"MESS? ".getBytes(),tmp.id.getBytes(),
				(" "+tmp.num_messString).getBytes(),"+++".getBytes());
		return tmp;
	}
	
	static Message MESS_POSITIVE() {
		byte[] MESS_array = new String("MESS>+++").getBytes();
		Message tmp = new Message(MESS_array, TypeMessage.MESSsup);
		return tmp;
	}
	
	static Message MESS_NEGATIVE() {
		byte[] MESS_array = new String("MESS<+++").getBytes();
		Message tmp = new Message(MESS_array, TypeMessage.MESSinf);
		return tmp;
	}
	
	static Message MENUM(int num, String mess)throws ParseException, NumberOfBytesException {
		testLengthMess(mess);
		
		int sizeMess = mess.getBytes().length;
		byte[] MENUM_array = new byte[5+1+byteSizePort+1+sizeMess+3];
		
		Message tmp=new Message(MENUM_array,TypeMessage.MENUM);
		
		tmp.num=num;
		tmp.mess=mess;
		tmp.convertALL();
		remplirData(MENUM_array,"MENUM ".getBytes(),(tmp.numString+" ").getBytes(),
				tmp.mess.getBytes(),"+++".getBytes());
		return tmp;
	}
	
	static Message FLOO_INTERRO(String mess)throws ParseException, NumberOfBytesException {
		testLengthMess(mess);
		
		int sizeMess = mess.getBytes().length;
		byte[] FLOO_array = new byte[5+1+sizeMess+3];
		
		Message tmp=new Message(FLOO_array,TypeMessage.FLOOi);
		
		tmp.mess=mess;
		tmp.convertALL();
		remplirData(FLOO_array,"FLOO? ".getBytes(),tmp.mess.getBytes(),"+++".getBytes());
		return tmp;
	}
	
	
	static Message FLOO_POSITIVE() {
		byte[] FLOO_array = new String("FLOO>+++").getBytes();
		Message tmp = new Message(FLOO_array, TypeMessage.FLOOsup);
		return tmp;
	}
	
	static Message LIST_INTERRO() {
		byte[] LIST_array = new String("LIST?+++").getBytes();
		Message tmp = new Message(LIST_array, TypeMessage.LISTi);
		return tmp;
	}
	
	static Message RLIST(int num_item)throws ParseException, NumberOfBytesException {
		byte[] RLIST_array = new byte[5+1+byteSizeItem+3];
		
		Message tmp=new Message(RLIST_array,TypeMessage.RLIST);
		
		tmp.num_item=num_item;
		tmp.convertALL();
		remplirData(RLIST_array,"RLIST ".getBytes(),tmp.num_itemString.getBytes(),"+++".getBytes());
		return tmp;
	}
	
	static Message LINUM(String id)throws ParseException, NumberOfBytesException {
		byte[] LINUM_array = new byte[5+1+byteSizeId+3];
		
		Message tmp=new Message(LINUM_array,TypeMessage.LINUM);
		
		tmp.id=id;
		if(id.length()!=8){
			throw new ParseException();
		}
		tmp.convertALL();
		remplirData(LINUM_array,"LINUM ".getBytes(),tmp.id.getBytes(),"+++".getBytes());
		return tmp;
	}
	
	static Message CONSU() {
		byte[] CONSU_array = new String("CONSU+++").getBytes();
		Message tmp = new Message(CONSU_array, TypeMessage.CONSU);
		return tmp;
	}
	
	static Message SSEM_POSITIVE(String id, int num_mess)throws ParseException, NumberOfBytesException {
		byte[] SSEM_array = new byte[5+1+byteSizeId+1+byteSizePort+3];
		
		Message tmp=new Message(SSEM_array,TypeMessage.SSEMsup);
		tmp.id=id;
		if(id.length()!=8){
			throw new ParseException();
		}
		tmp.num_mess=num_mess;
		tmp.convertALL();
		remplirData(SSEM_array,"SSEM> ".getBytes(),tmp.id.getBytes(),
				(" "+tmp.num_messString).getBytes(),"+++".getBytes());
		return tmp;
	}
	
	
	
	static Message MUNEM(int num, String mess)throws ParseException, NumberOfBytesException {
		testLengthMess(mess);
		
		int sizeMess = mess.getBytes().length;
		
		byte[] MUNEM_array = new byte[5+1+byteSizePort+1+sizeMess+3];
		
		Message tmp=new Message(MUNEM_array,TypeMessage.MUNEM);
		
		tmp.num=num;
		tmp.mess=mess;
		tmp.convertALL();
		remplirData(MUNEM_array,"MUNEM ".getBytes(),(tmp.numString+" ").getBytes(),
				tmp.mess.getBytes(),"+++".getBytes());
		return tmp;
	}
	
	static Message OOLF_POSITIVE(String id, String mess)throws ParseException, NumberOfBytesException {
		testLengthMess(mess);
		
		int sizeMess = mess.getBytes().length;
		byte[] OOLF_array = new byte[5+1+byteSizeId+1+sizeMess+3];
		
		Message tmp=new Message(OOLF_array,TypeMessage.OOLFsup);
		tmp.id=id;
		if(id.length()!=8){
			throw new ParseException();
		}
		tmp.mess=mess;
		tmp.convertALL();
		remplirData(OOLF_array,"OOLF> ".getBytes(),tmp.id.getBytes(),
				(" "+tmp.mess).getBytes(),"+++".getBytes());
		return tmp;
	}
	
	static Message EIRF_POSITIVE(String id)throws ParseException, NumberOfBytesException {
		byte[] EIRF_array = new byte[5+1+byteSizeId+3];
		
		Message tmp=new Message(EIRF_array,TypeMessage.EIRFsup);
		tmp.id=id;
		if(id.length()!=8){
			throw new ParseException();
		}
		tmp.convertALL();
		remplirData(EIRF_array,"EIRF> ".getBytes(),tmp.id.getBytes(),"+++".getBytes());
		return tmp;
	}
	
	static Message OKIRF() {
		byte[] OKIRF_array = new String("OKIRF+++").getBytes();
		Message tmp = new Message(OKIRF_array, TypeMessage.OKIRF);
		return tmp;
	}
	
	static Message NOKRF() {
		byte[] NOKRF_array = new String("NOKRF+++").getBytes();
		Message tmp = new Message(NOKRF_array, TypeMessage.NOKRF);
		return tmp;
	}
	
	static Message FRIEN(String id)throws ParseException, NumberOfBytesException {
		byte[] FRIEN_array = new byte[5+1+byteSizeId+3];
		
		Message tmp=new Message(FRIEN_array,TypeMessage.FRIEN);
		tmp.id=id;
		if(id.length()!=8){
			throw new ParseException();
		}
		tmp.convertALL();
		remplirData(FRIEN_array,"FRIEN ".getBytes(),tmp.id.getBytes(),"+++".getBytes());
		return tmp;
	}
	
	static Message NOFRI(String id)throws ParseException, NumberOfBytesException {
		byte[] NOFRI_array = new byte[5+1+byteSizeId+3];
		
		Message tmp=new Message(NOFRI_array,TypeMessage.NOFRI);
		tmp.id=id;
		if(id.length()!=8){
			throw new ParseException();
		}
		tmp.convertALL();
		remplirData(NOFRI_array,"NOFRI ".getBytes(),tmp.id.getBytes(),"+++".getBytes());
		return tmp;
	}
	
	static Message LBUP_POSITIVE(String ip_diff, int port, String mess) throws ParseException, NumberOfBytesException{
		testLengthMess(mess);
		int sizeMess = mess.getBytes().length;
		byte[] LBUP_array = new byte[5+1+byteSizeIP+1+byteSizePort+1+sizeMess+3];
		
		Message tmp=new Message(LBUP_array,TypeMessage.LBUPsup);
		tmp.ip_diff=ip_diff;
		tmp.mess=mess;
		tmp.port=port;
		tmp.convertALL();
		remplirData(LBUP_array,"LBUP> ".getBytes(),tmp.ip_diff.getBytes(),
				(" "+tmp.portString+" ").getBytes(),tmp.mess.getBytes(), "+++".getBytes());
		return tmp;
	}
	
	static Message NOCON() {
		byte[] NOCON_array = new String("NOCON+++").getBytes();
		Message tmp = new Message(NOCON_array, TypeMessage.NOCON);
		return tmp;
	}
	
	static Message IQUIT() {
		byte[] IQUIT_array = new String("IQUIT+++").getBytes();
		Message tmp = new Message(IQUIT_array, TypeMessage.IQUIT);
		return tmp;
	}
	
	static Message PROM(String prom_mess)throws ParseException, NumberOfBytesException {
		if(prom_mess.getBytes().length >maxPromMsg){
			throw new ParseException();		
		}
		
		int sizeMess = prom_mess.getBytes().length;
		byte[] PROM_array = new byte[4+1+sizeMess];
		
		Message tmp=new Message(PROM_array,TypeMessage.PROM);
		
		tmp.prom_mess=prom_mess;
		tmp.convertALL();
		remplirData(PROM_array,"PROM ".getBytes(),tmp.prom_mess.getBytes());
		return tmp;
	}
	
	static Message PUBL_INTERRO(String ip_diff, int port, String mess) throws ParseException, NumberOfBytesException{
		
		testLengthMess(mess);
		int sizeMess = mess.getBytes().length;
		byte[] PUBL_array = new byte[5+1+byteSizeIP+1+byteSizePort+1+sizeMess+3];
		
		Message tmp=new Message(PUBL_array,TypeMessage.PUBLi);
		tmp.ip_diff=ip_diff;
		tmp.mess=mess;
		tmp.port=port;
		tmp.convertALL();
		remplirData(PUBL_array,"PUBL? ".getBytes(),tmp.ip_diff.getBytes(),
				(" "+tmp.portString+" ").getBytes(),tmp.mess.getBytes(), "+++".getBytes());
		return tmp;
	}
	
	static Message PUBL_POSITIVE() {
		byte[] PUBL_array = new String("PUBL>+++").getBytes();
		Message tmp = new Message(PUBL_array, TypeMessage.PUBLsup);
		return tmp;
	}
	
	
	static Message XYY(byte y , char xx) throws ParseException, NumberOfBytesException {
		
		
		byte[] XYY_array = new byte[3];
		Message tmp = new Message(XYY_array, TypeMessage.YXX);
		
		tmp.Y=y;
		tmp.XX=xx;
		
		byte y_array []= new byte[1];
		y_array[0]=y;
		tmp.convertALL();
		remplirData(XYY_array,y_array,tmp.XXLITTLE_ENDIAN_2);
		return tmp;

	}
	
	
	
	
	static void testLengthMess(String mess)throws ParseException{
		if(mess.getBytes().length >maxMsg){
			throw new ParseException();		
		}
	}
	
	/**
	 * Rempli data avec les args
	 * @param args
	 */
	public static void remplirData(byte [] data ,byte[]... args) {
		int i = 0;
		for (byte[] arg1 : args) {
			for (byte arg2 : arg1) {
				data[i] = arg2;
				i++;
			}
		}
	}
	
	/**
	 * 
	 * Cree un String de la valeur 562 sur 6 -> 000562
	 * 
	 * @param value
	 * @param numberOfBytes
	 * @return 
	 * @throws Exception
	 */
	public static String intToStringRepresentation(int value,int numberOfBytes) throws NumberOfBytesException{
		if(value<0){
			throw new NumberOfBytesException();
		}
		int numberOfZERO = numberOfBytes - (Long.toString(value)).length();
		if(numberOfZERO<0){
			throw new NumberOfBytesException();
		}
		String tmp="";
		for(int i=0;i<numberOfZERO;i++){
			tmp=tmp+"0";
		}
		tmp=tmp+value;
		return tmp;
	}
	
	public static byte[] charToByteArray(char val,int numberOfByte,ByteOrder ENDIAN){
		if(val<0){
		}
		return ByteBuffer.allocate(numberOfByte).order(ENDIAN).putChar(val).array();
	}
	
	public static char byteArrayToChar(byte[] bytes ,int numberOfByte,ByteOrder ENDIAN){
		ByteBuffer buffer = ByteBuffer.allocate(numberOfByte).order(ENDIAN);
		buffer.put(bytes);
		buffer.flip();//need flip 
		return buffer.getChar();
	}
	
	/**
	 * Convertir un port 45 -> 0045
	 * @param port
	 * @return
	 * @throws Exception
	 */
	private String convertPort(int port) throws ParseException{
		
		int size=(""+port).length();
		if(size>4 || port<0){
			throw new ParseException();
		}
		int diff=4-size;
		String result=(""+port);
		for(int i=0;i<diff;i++){
			result="0"+result;
		}
		return result;
	}
	
	/**
	 * Convertir un num_item 45 -> 045
	 * @param num_item
	 * @return
	 * @throws Exception
	 */
	private String convertItem(int num_item) throws ParseException{
		
		int size=(""+num_item).length();
		if(size>3 || num_item<0){
			throw new ParseException();
		}
		int diff=3-size;
		String result=(""+num_item);
		for(int i=0;i<diff;i++){
			result="0"+result;
		}
		return result;
	}
	
	
	
	
	/**
	 * Convertir une ip 192.0.0.1 -> 192.000.000.001
	 * @param ip
	 * @return 
	 * @throws Exception
	 */
	public static String convertIP(String ip) throws ParseException{
		
		if(ip.equals("localhost")){
			return "127.0.0.1######";
		}
		
		String[]tmp=ip.split("\\.");
		
		if(tmp.length!=4){
			throw new ParseException();
		}
		//to put the #
		String tmp2 ="";
		for(int i=0; i<4;i++){
			if(tmp[i].length()==2){
				tmp2=tmp2+"#";
			}
			else if(tmp[i].length()==1){
				tmp2= tmp2+"##";
			}
		}
		String res=tmp[0]+"."+tmp[1]+"."+tmp[2]+"."+tmp[3]+tmp2;
		
		return res;
	}
	
	
	
	public boolean isMulti() {
		return multi;
	}
	public void setMulti(boolean multi) {
		this.multi = multi;
	}
	public byte[] getData() {
		return data;
	}
	public TypeMessage getType() {
		return type;
	}
	public String getIp_diff() {
		return ip_diff;
	}
	public String getMsg() {
		return mess;
	}
	public String getId_app() {
		return id_app;
	}
	public byte[] getData_app() {
		return data_app;
	}
	public String getIp_succ() {
		return ip_succ;
	}
	public Integer getPort(){
		return this.port;
	}
	public char getMdp(){
		return this.mdp;
	}

	public String getId() {
		return id;
	}
	public Integer getNum_mess(){
		return this.num_mess;
	}
	
	public Integer getNum(){
		return this.num;
	}
	
	public Integer getNumItem(){
		return this.num_item;
	}

}
