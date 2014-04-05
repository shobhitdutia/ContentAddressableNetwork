/* 
 * Peer.java 
 * 
 * Version: 
 *     $Id$ 
 * 
 * Revisions: 
 *     $Log$ 
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

/**
 * This program is the peer node in CAN
 *
 * @author      Shobhit Dutia
 */

public class Peer extends Thread{
	static Coordinate currentCoordinate;
	static Map<String, Coordinate> neighborMap=new HashMap<String, Coordinate> ();
	static Set<String> keywordSet=new HashSet<String>();
	static String currentIp;
	ServerSocket ss;
	public Peer() {
		currentCoordinate=new Coordinate();
		currentCoordinate.lx=0;
		currentCoordinate.ly=0;
		currentCoordinate.hx=0;
		currentCoordinate.hy=0;
	}

	/**
	 * The main program.
	 * 
	 * @param    args  	command line arguments (ignored)  
	 */
	
	public static void main(String[] args) throws UnknownHostException {
		Scanner sc=new Scanner(System.in);
		int ch;
		Peer p=new Peer();
		System.out.println("1. join");
		System.out.println("2. exit");
		ch=sc.nextInt();
		switch(ch) {
		case 1: 
		try {
			System.out.println("Enter IP address of bootstrap server ");
			String bootStrapIp=sc.next();
			p.joinSystem(bootStrapIp);
			System.out.println("Join successful! Starting to listen ...");
			p.start();   //start listening after peer has joined the system
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		break;
		case 2: 
			System.out.println("Good bye");
			System.exit(0);
		break;
		default:
			System.out.println("Wrong choice! Enter again");
		break;
		}
		boolean flag=true;
		while(flag) {
			System.out.println("1. Insert");
			System.out.println("2. Leave");
			System.out.println("3. Search");
			System.out.println("4. Display peer info");
			ch=sc.nextInt();
			try {
			switch(ch) {
				case 1:
					p.insert();
					break;
				case 2:
					flag=false;
					p.leave();
					break;
				case 3:
					p.search();
					break;
				case 4: 
					p.displayInfo();
					break;
				}
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		sc.close();   
		System.exit(0);
	}
	
	private void leave() throws UnknownHostException, IOException {
		//Vector<String> neighborsTobeNotifiedforDeletion=new Vector<String>();
		Map<String, Coordinate> neighborsTobeNotifiedforDeletion=new HashMap<String, Coordinate> ();
		String leavingNodePosition=null;
		for(Map.Entry<String, Coordinate> entry: neighborMap.entrySet()) {
			Coordinate neighbor=entry.getValue();
			Coordinate neighborCoordinate=new Coordinate();
			//check if removing the node forms a rectangle or square
			if((currentCoordinate.ly==neighbor.ly&&currentCoordinate.hy==neighbor.hy)||
					(currentCoordinate.lx==neighbor.lx&&currentCoordinate.hx==neighbor.hx)) {
				//check if 2 zones having same ly and hy are merged
				if(currentCoordinate.ly==neighbor.ly&&currentCoordinate.hy==neighbor.hy) {
					//check if node which is leaving is left rectangle 
					if(currentCoordinate.lx<neighbor.lx) {
						leavingNodePosition="left";
						System.out.println("My neighbor map is" +neighborMap);
						for(Map.Entry<String, Coordinate> entry1: neighborMap.entrySet()) {
							Coordinate neighbor1=entry1.getValue();
							if(entry1.getValue()==entry.getValue()) {
								//continue;
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());
							}
							//inform all top common, bottom common and left nodes to delete current node and inform about new merged node
							
							//check for top both
							if(currentCoordinate.hy==neighbor1.ly&&neighbor1.lx<(currentCoordinate.hx)
									&&neighbor1.hx>currentCoordinate.hx) {
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());
							}
							//check for bottom both
							else if(currentCoordinate.ly==neighbor1.hy&&neighbor1.lx<(currentCoordinate.hx)
								&&neighbor1.hx>currentCoordinate.hx) {
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());
							}
							//check for left
							else if(currentCoordinate.lx==neighbor1.hx) {
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());									
							}
							//special case top left
							else if(currentCoordinate.hy==neighbor1.ly&&neighbor1.hx<=currentCoordinate.hx) {
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());							
							}
							//special case bottom left
							else if(currentCoordinate.ly==neighbor1.hy&&neighbor1.hx<=currentCoordinate.hx) {
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());							
							}
						}
						//update neighbor's coordinate and send it
						neighborCoordinate.ly=currentCoordinate.ly;
						neighborCoordinate.hy=currentCoordinate.hy;
						neighborCoordinate.lx=currentCoordinate.lx;
						neighborCoordinate.hx=neighbor.hx;
					}//end of left rectangle check	
					//or check if leaving node is a right side rectangle node
					else if(currentCoordinate.lx>neighbor.lx) {
						leavingNodePosition="right";
						System.out.println("My neighbor map is" +neighborMap);
						for(Map.Entry<String, Coordinate> entry1: neighborMap.entrySet()) {
							Coordinate neighbor1=entry1.getValue();
							if(entry1.getValue()==entry.getValue()) {
								//continue;
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());
							}
							//inform all top common, bottom common and right nodes to delete current node and inform about new merged node
							
							//check for top both
							if(currentCoordinate.hy==neighbor1.ly&&neighbor1.lx<(currentCoordinate.lx)
									&&neighbor1.hx>currentCoordinate.lx) {
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());
							}
							//check for bottom both
							else if(currentCoordinate.ly==neighbor1.hy&&neighbor1.lx<(currentCoordinate.lx)
								&&neighbor1.hx>currentCoordinate.lx) {
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());
							}
							//check for right
							else if(currentCoordinate.hx==neighbor1.lx) {
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());							
							}
							//special case top right
							else if(currentCoordinate.hy==neighbor1.ly&&neighbor1.lx>=currentCoordinate.lx) {
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());							
							}
							//special case bottom right
							else if(currentCoordinate.ly==neighbor1.hy&&neighbor1.lx>=currentCoordinate.lx) {
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());							
							}
						}
						neighborCoordinate.ly=currentCoordinate.ly;
						neighborCoordinate.hy=currentCoordinate.hy;
						neighborCoordinate.lx=neighbor.lx;
						neighborCoordinate.hx=currentCoordinate.hx;
					}//end of right rectangle check
				}
				//check if 2 zones having same lx and hx are merged
				else if(currentCoordinate.lx==neighbor.lx&&currentCoordinate.hx==neighbor.hx) {
					//--------------------------------------------------------------//
					//check if node which is leaving is bottom rectangle 
					if(currentCoordinate.ly<neighbor.ly) {
						leavingNodePosition="bottom";
						System.out.println("My neighbor map is" +neighborMap);
						for(Map.Entry<String, Coordinate> entry1: neighborMap.entrySet()) {
							Coordinate neighbor1=entry1.getValue();
							if(entry1.getValue()==entry.getValue()) {
								//continue;
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());
							}
							//inform all left common, right common and bottom nodes to delete current node and inform about new merged node
							
							//check for left both
							if(currentCoordinate.lx==neighbor1.hx&&neighbor1.ly<(currentCoordinate.hy)
									&&neighbor1.hy>currentCoordinate.hy) {
								System.out.println("left both true");
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());
							}
							//check for right both
							else if(currentCoordinate.hx==neighbor1.lx&&neighbor1.ly<(currentCoordinate.hy)
									&&neighbor1.hy>currentCoordinate.hy) {
								System.out.println("right both");
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());
							}
							//check for bottom
							else if(currentCoordinate.ly==neighbor1.hy) {
								System.out.println("bottom true");
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());							
							}
							//special case bottom right
							else if(currentCoordinate.hx==neighbor1.lx&&neighbor1.hy<=currentCoordinate.hy) {
								System.out.println("bottom right true");
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());							
							}
							//special case bottom left 
							else if(currentCoordinate.lx==neighbor1.hx&&neighbor1.hy<=currentCoordinate.hy) {
								System.out.println("bottom left true");
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());							
							}
						}
						//update neighbor's coordinate and send it
						neighborCoordinate.lx=currentCoordinate.lx;
						neighborCoordinate.hx=currentCoordinate.hx;
						neighborCoordinate.ly=currentCoordinate.ly;
						neighborCoordinate.hy=neighbor.hy;
					}//end of left rectangle check	
					//or check if leaving node is a top rectangle node
					else if(currentCoordinate.ly>neighbor.ly) {
						leavingNodePosition="top";
						System.out.println("My neighbor map is" +neighborMap);
						for(Map.Entry<String, Coordinate> entry1: neighborMap.entrySet()) {
							Coordinate neighbor1=entry1.getValue();
							System.out.println("checking same");
							if(entry1.getValue()==entry.getValue()) {
								System.out.println("Same true");
								//continue;
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());
							}
							//inform all left common, right common and top nodes to delete current node and inform about new merged node
							
							//check for left both
							else if(currentCoordinate.lx==neighbor1.hx&&neighbor1.ly<(currentCoordinate.ly)
									&&neighbor1.hy>currentCoordinate.ly) {
								System.out.println("left both true");
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());
							}
							//check for right both
							else if(currentCoordinate.hx==neighbor1.lx&&neighbor1.ly<(currentCoordinate.ly)
									&&neighbor1.hy>currentCoordinate.ly) {
								System.out.println("right both true");
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());
							}
							//check for top
							else if(currentCoordinate.hy==neighbor1.ly) {
								System.out.println("top true");
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());								
							}
							//special case top right
							else if(currentCoordinate.hx==neighbor1.lx&&neighbor1.ly>=currentCoordinate.ly) {
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());							
							}
							//special case top left 
							else if(currentCoordinate.lx==neighbor1.hx&&neighbor1.ly>=currentCoordinate.ly) {
								neighborsTobeNotifiedforDeletion.put(entry1.getKey(),entry1.getValue());							
							}
						}
						neighborCoordinate.lx=currentCoordinate.lx;
						neighborCoordinate.hx=currentCoordinate.hx;
						neighborCoordinate.ly=neighbor.ly;
						neighborCoordinate.hy=currentCoordinate.hy;
					}//end of right rectangle check
					//--------------------------------------------------------------//
				}
				System.out.println(leavingNodePosition);
				//inform neighbor list for deletion of current node
				for(String ipaddressOfNeighbor:neighborsTobeNotifiedforDeletion.keySet()) {
					Socket neighborSocket=new Socket(ipaddressOfNeighbor,8080);
					String deleteCurrentNode="true";
					System.out.println("sent "+deleteCurrentNode+" update to ip"+ipaddressOfNeighbor);
					ObjectOutputStream out = new ObjectOutputStream (neighborSocket.getOutputStream ());
					out.writeObject("update");
					out.writeObject(currentCoordinate); //sending coordinates of node to all its neighbors 
					out.writeObject(currentIp);
					out.writeObject(deleteCurrentNode);
					neighborSocket.close();
				}

				//contact neighbor node to update  its own neighbors and neighbors which current node deleted 
				Socket neighborSocket=new Socket(entry.getKey(),8080);
				neighborsTobeNotifiedforDeletion.remove(entry.getKey());
				ObjectOutputStream out = new ObjectOutputStream (neighborSocket.getOutputStream ());
				out.writeObject("updateNeighborForMerge");
				out.writeObject(neighborCoordinate); //sending updated coordinates to neighbor
				//send deleted neighbor list to neighbor so that it can add those 
				out.writeObject(neighborsTobeNotifiedforDeletion);
				out.writeObject(leavingNodePosition);
				neighborSocket.close();	
				System.out.println("Node leave successful");
				break;
			}
		}//end of for loop
	}//end of leave
	
	
	/**
	 * Search function.
	 *     
	 * @exception UnknownHostException
	 * @exception IOException
	 * @exception ClassNotFoundException
	 *     
	 */
	
	private void search() throws UnknownHostException, IOException, ClassNotFoundException {
		Scanner sc=new Scanner(System.in);
		System.out.println("Enter the name of the keyword which you want to search");
		String keyword=sc.next();
		double x=0, y=0;
		System.out.println("keyword is "+keyword);
		char character;
		for (int i = 0; i < keyword.length(); i++) {
			character=keyword.charAt(i);
			if(i%2!=0) {
				x+=((double)character);
			}
			else {
				y=y+((double)character);	
			}
		}
		x=x%10;
		y=y%10;
		System.out.println("Searching Keyword on x:"+x+" y:"+y);
		//sc.close();
		//check if coordinates are in the region
		if((x>=currentCoordinate.lx&&x<=currentCoordinate.hx)&&(y>=currentCoordinate.ly&&y<=currentCoordinate.hy)) {   
			if(keywordSet.contains(keyword)) {
				System.out.println("Keyword "+keyword+" found on current machine" );			
			}
			else {
				System.out.println("Failure!");
			}
		}
		else {
			String ipaddressOfNeighbor=computeMin(x,y);
			Socket sock=new Socket(ipaddressOfNeighbor, 8080);
			System.out.println("Forwarding search request to "+ipaddressOfNeighbor+" to reach x:"+x+", y:"+y);		
			ObjectOutputStream out = new ObjectOutputStream (sock.getOutputStream ());
			out.writeObject("search");
			out.writeObject(x);
			out.writeObject(y);
			out.writeObject(currentIp);
			out.writeObject(keyword);
			sock.close();
			//client requesting ack of insert 
			ServerSocket clientServerSocket =new ServerSocket(8081);   //note different port number in use
			Socket acceptDataFromServer=clientServerSocket.accept();
			ObjectInputStream in = new ObjectInputStream(acceptDataFromServer.getInputStream());
			String responsefromPeer=(String)in.readObject();
			System.out.println(responsefromPeer);
			acceptDataFromServer.close();
			clientServerSocket.close();	
		}
	}
	
	/**
	 * Insert function.
	 * 
	 * @exception UnknownHostException
	 * @exception IOException
	 * @exception ClassNotFoundException
	 */
	
	private void insert() throws UnknownHostException, IOException, ClassNotFoundException {
		Scanner sc=new Scanner(System.in);
		System.out.println("Enter the name of the keyword which you want to insert");
		String keyword=sc.next();
		double x=0, y=0;
		char character;
		for (int i = 0; i < keyword.length(); i++) {
			character=keyword.charAt(i);
			if(i%2!=0) {
				x+=((double)character)%10;
			}
			else {
				y=y+((double)character)%10;	
			}
			x=x%10;
			y=y%10;
		}
		System.out.println("Insertion target coordinates x:"+x+" y:"+y);
		//check if coordinates are in the region
		if((x>=currentCoordinate.lx&&x<=currentCoordinate.hx)&&(y>=currentCoordinate.ly&&y<=currentCoordinate.hy)) {   
			keywordSet.add(keyword);
			System.out.println("Inserting keyword "+keyword+" on current zone");		
		}
		else {
			String ipaddressOfNeighbor=computeMin(x,y);
			Socket sock=new Socket(ipaddressOfNeighbor, 8080);
			//give the designated file coordinates to target peer
			System.out.println("Forwarding inserting request to "+ipaddressOfNeighbor+" to reach x:"+x+", y:"+y);		
			ObjectOutputStream out = new ObjectOutputStream (sock.getOutputStream ());
			out.writeObject("insert");
			out.writeObject(x);
			out.writeObject(y);
			out.writeObject(currentIp);
			out.writeObject(keyword);
			sock.close();
			//client requesting ack of insert 
			ServerSocket clientServerSocket =new ServerSocket(8081);   //note different port number in use
			Socket acceptDataFromServer=clientServerSocket.accept();
			ObjectInputStream in = new ObjectInputStream(acceptDataFromServer.getInputStream());
			String responsefromPeer=(String)in.readObject();
			System.out.println(responsefromPeer);
			acceptDataFromServer.close();
			clientServerSocket.close();	
		}
	}
	
	/**
	 * Display peer information.
	 * 
	 * @exception UnknownHostException
	 * 
	 */
	
	private void displayInfo() throws UnknownHostException {
		System.out.println("IP address: "+InetAddress.getLocalHost().getHostAddress());
		System.out.println("Coordinates of peer are:");
		System.out.print("lx: "+currentCoordinate.lx+" ");
		System.out.print("hx: "+currentCoordinate.hx+" ");
		System.out.print("ly: "+currentCoordinate.ly+" ");
		System.out.print("hy: "+currentCoordinate.hy+"\n");
		System.out.println("Neighbouring coordinates are:");    
		//System.out.println(neighborMap);
		for(Map.Entry<String, Coordinate> entry: neighborMap.entrySet()) {
			Coordinate neighbor=entry.getValue();
			System.out.print("lx: "+neighbor.lx+" ");
			System.out.print("hx: "+neighbor.hx+" ");
			System.out.print("ly: "+neighbor.ly+" ");
			System.out.print("hy: "+neighbor.hy+"\n");
		}
	}
	
	/**
	 * Thread run method.
	 *  
	 */
	
	public void run() {
		try {
			this.listenToConnections();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * join method.
	 * @param bootStrapIp 
	 * 
	 * @exception UnknownHostException
	 * @exception ClassNotFoundException
	 * 
	 */
	
	@SuppressWarnings("unchecked")
	public void joinSystem(String bootStrapIp) throws UnknownHostException, ClassNotFoundException {
		currentIp=InetAddress.getLocalHost().getHostAddress();
		//select a random point
		Random r=new Random();
		double x=(double)r.nextInt(11), y=(double)r.nextInt(11);
		System.out.println("Random point:"+x+", "+y);
		try {//contact bootstrap server
            //JoinInterface obj=(JoinInterface)Naming.lookup("//129.21.37.28:12459/Shobhit_bootstrapObject");	    
			JoinInterface obj=(JoinInterface)Naming.lookup("//"+bootStrapIp+":12459/Shobhit_bootstrapObject");	    
			String ipaddressOfBootStrapNode=obj.joinRequest(currentIp);    //get ip address of bootstrap node from bootstrap server
			if(ipaddressOfBootStrapNode!=null) { //if node is not the 1st one to join
			//connect to bootstrap node
				System.out.println("Connecting to bootstrap node!"+ipaddressOfBootStrapNode);
				Socket sock=new Socket(ipaddressOfBootStrapNode, 8080);
				//give the random coordinates selected by the joining node and IP(to keep track of neighbouring node) to the bootstrap node for routing
				System.out.println("Sending x:"+x+", y:"+y+" ip="+currentIp);
				ObjectOutputStream out = new ObjectOutputStream (sock.getOutputStream ());
				out.writeObject("join");
				out.writeObject(x);
				out.writeObject(y);
				out.writeObject(currentIp);
				sock.close();
				ServerSocket clientServerSocket =new ServerSocket(8080);   //client actin as a server to accept neighbor list and coordinates
				Socket acceptDataFromServer=clientServerSocket.accept();
				ObjectInputStream in = new ObjectInputStream(acceptDataFromServer.getInputStream());
				neighborMap=(Map<String, Coordinate>) in.readObject();   //get neighbor list
				currentCoordinate=(Coordinate) in.readObject(); //get current coordinates
				keywordSet=(Set<String>)in.readObject();
				acceptDataFromServer.close();
				clientServerSocket.close();
			}
			else {
			//node owns whole zone since its the 1st peer
				System.out.println("I am the first peer!");
				currentCoordinate.hx=10;
				currentCoordinate.hy=10;
			}
		} catch (NotBoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Listen to incoming connections.
	 * 
	 * @exception IOException
	 * @exception ClassNotFoundException
	 * 
	 */
	
	@SuppressWarnings("unchecked")
	public void listenToConnections() throws IOException, ClassNotFoundException {
		
		ss=new ServerSocket(8080);
		while(true) {
			//System.out.println("\nI am listening for incoming requests on port 8080");
			Socket client=ss.accept();
			//System.out.println("Incoming request from peer!");
			ObjectInputStream in = new ObjectInputStream(client.getInputStream());
			String requestType=(String)in.readObject();
			String ipaddressOfClient=null;
			double x=0, y = 0;
			if(requestType.equals("join")) {
				x=(Double)in.readObject();
				y=(Double)in.readObject();
				ipaddressOfClient= (String) in.readObject();
			}
			else if(requestType.equals("update")) {
				Coordinate updatedCoordinate=(Coordinate)in.readObject();
				String incomingIpAddress=(String) in.readObject();
				String deleteNeighborNode=(String) in.readObject();
				System.out.println("--------"+deleteNeighborNode);
				if(deleteNeighborNode.equals("true")) {
					//System.out.println("went into true.. deleting incoming ip"+incomingIpAddress);
					neighborMap.remove(incomingIpAddress);					
				}
				else {
					neighborMap.put(incomingIpAddress, updatedCoordinate);
				}
				client.close();
				continue;
			}
			
			else if(requestType.equals("updateNeighborForMerge")) {
				Coordinate updatedCoordinateOfMyself=(Coordinate)in.readObject();
				//currentCoordinate=updatedCoordinateOfMyself;
				Map<String, Coordinate> neighborsTobeNotifiedforAddition=new HashMap<String, Coordinate> ();
				neighborsTobeNotifiedforAddition=(Map<String,Coordinate>) in.readObject();
				String leavingNodePosition=(String) in.readObject();
				client.close();
				
				Vector<String> neighborsTobeNotifiedforAdditionVector=new Vector<String>();
				//add peers neighbors as own and get a list of all neighbors to be notified in a vector
				for(Map.Entry<String, Coordinate> entry: neighborsTobeNotifiedforAddition.entrySet()) {
					neighborMap.put(entry.getKey(), entry.getValue());
					neighborsTobeNotifiedforAdditionVector.add(entry.getKey());
				}
				
				//contact original neighbor list and previous node's deleted neighbor list to update new coordinates
				for(Map.Entry<String, Coordinate> entry: neighborMap.entrySet()) {
					Coordinate neighbor=entry.getValue();
					if(neighborsTobeNotifiedforAdditionVector.contains(entry.getKey())) {
						continue;
					}
					if(leavingNodePosition=="left"||leavingNodePosition=="right") {
						//top check
						if(currentCoordinate.hy==neighbor.ly) {
							System.out.println("addedT");
							neighborsTobeNotifiedforAdditionVector.add(entry.getKey());
						}
						//bottom check
						else if(currentCoordinate.ly==neighbor.hy) {
							System.out.println("addedB");
							neighborsTobeNotifiedforAdditionVector.add(entry.getKey());
						}
						//right check or left check
						else if(leavingNodePosition=="left") {
							if(currentCoordinate.hx==neighbor.lx) {
								System.out.println("addedL");
								neighborsTobeNotifiedforAdditionVector.add(entry.getKey());
							}								
						}
						else if(leavingNodePosition=="right") {
							System.out.println("addedR");
							if(currentCoordinate.lx==neighbor.hx) {
								neighborsTobeNotifiedforAdditionVector.add(entry.getKey());
							}								
						}
					}
					else if(leavingNodePosition=="top"||leavingNodePosition=="bottom") {
						//left check
						if(currentCoordinate.lx==neighbor.hx) {
							System.out.println("addedL");
							neighborsTobeNotifiedforAdditionVector.add(entry.getKey());
						}
						//right check
						else if(currentCoordinate.hx==neighbor.lx) {
							System.out.println("addedR");
							neighborsTobeNotifiedforAdditionVector.add(entry.getKey());
						}
						//top or bottom check
						else if(leavingNodePosition=="bottom") {
							System.out.println("addedB");
							if(currentCoordinate.hy==neighbor.ly) {
								neighborsTobeNotifiedforAdditionVector.add(entry.getKey());
							}								
						}
						else if(leavingNodePosition=="top") {
							System.out.println("addedT");
							if(currentCoordinate.ly==neighbor.hy) {
								neighborsTobeNotifiedforAdditionVector.add(entry.getKey());
							}								
						}
					}
					
				}
				System.out.println(neighborsTobeNotifiedforAdditionVector+" basdasdasdasdsadooo yeahhhhhhh");
				//update current coordinates
				currentCoordinate=updatedCoordinateOfMyself;
				for(String ipaddressOfNeighbor:neighborsTobeNotifiedforAdditionVector) {
					Socket neighborSocket=new Socket(ipaddressOfNeighbor,8080);
					String deleteCurrentNode="false";
					ObjectOutputStream out = new ObjectOutputStream (neighborSocket.getOutputStream ());
					out.writeObject("update");
					out.writeObject(currentCoordinate); //sending coordinates of node to all its neighbors 
					out.writeObject(currentIp);
					out.writeObject(deleteCurrentNode);
					neighborSocket.close();
				}
				continue;
			}
			
			else if(requestType.equals("insert")) {
				x=(Double)in.readObject();
				y=(Double)in.readObject();
				ipaddressOfClient=(String) in.readObject();
				String keyword= (String) in.readObject();
				if((x>=currentCoordinate.lx&&x<=currentCoordinate.hx)&&(y>=currentCoordinate.ly&&y<=currentCoordinate.hy)) {   //check if incoming node request coordinate are in the region
					keywordSet.add(keyword);
					Socket sock=new Socket(ipaddressOfClient, 8081);//note different port number in use  
					System.out.println("Inserted keyword "+keyword+" successfully on peer "+currentIp);		
					ObjectOutputStream out = new ObjectOutputStream (sock.getOutputStream ());
					out.writeObject("Inserted keyword "+keyword+" successfully on peer "+currentIp);		
					sock.close();
				}
				else {
					String ipaddressOfNeighbor=computeMin(x,y);
					Socket sock=new Socket(ipaddressOfNeighbor, 8080);   
					//give the designated file coordinates to target peer
					System.out.println("Routing insert request to neighbor for x:"+x+", y:"+y);		
					ObjectOutputStream out = new ObjectOutputStream (sock.getOutputStream ());
					out.writeObject("insert");
					out.writeObject(x);
					out.writeObject(y);
					out.writeObject(ipaddressOfClient);
					out.writeObject(keyword);
					sock.close();
				}
				continue;
			}
			else if(requestType.equals("search")) {
				x=(Double)in.readObject();
				y=(Double)in.readObject();
				ipaddressOfClient=(String) in.readObject();
				String keyword= (String) in.readObject();
				if((x>=currentCoordinate.lx&&x<=currentCoordinate.hx)&&(y>=currentCoordinate.ly&&y<=currentCoordinate.hy)) {   //check if incoming node request coordinate are in the region
					String result=String.valueOf(keywordSet.contains(keyword));					
					if(result.equals("true")) {
						result="Found on peer "+currentIp;
					}
					else {
						result="Failure on "+currentIp;
					}
					System.out.println(result);
					Socket sock=new Socket(ipaddressOfClient, 8081); //note different port number in use
					ObjectOutputStream out = new ObjectOutputStream (sock.getOutputStream ());
					out.writeObject(result);		
					sock.close();	
				}
				else {
					String ipaddressOfNeighbor=computeMin(x,y);
					Socket sock=new Socket(ipaddressOfNeighbor, 8080);
					//give the designated file coordinates to target peer
					System.out.println("Routing search request to neighbor for x:"+x+", y:"+y);		
					ObjectOutputStream out = new ObjectOutputStream (sock.getOutputStream ());
					out.writeObject("search");
					out.writeObject(x);
					out.writeObject(y);
					out.writeObject(ipaddressOfClient);
					out.writeObject(keyword);
					sock.close();
				}
				continue;
			}
			//System.out.println(x+" "+y+" "+ipaddressOfClient);
			client.close();
			//check if current node has to be splitted
			if((x>=currentCoordinate.lx&&x<=currentCoordinate.hx)&&(y>=currentCoordinate.ly&&y<=currentCoordinate.hy)) {   //check if incoming node request coordinate are in the region
				Socket newPeerSocket=new Socket(ipaddressOfClient,8080);
				ObjectOutputStream out = new ObjectOutputStream (newPeerSocket.getOutputStream ());
				Coordinate neighborCoordinate=new Coordinate();
				System.out.println("Splitting current zone");
				out.writeObject(splitzone(neighborCoordinate, ipaddressOfClient)); //split zone and send neighbor's information to calling node
				out.writeObject(neighborCoordinate);
				out.writeObject(updateKeywordList());
				newPeerSocket.close();
			}
			else { 
				//for all neighbours, check which neighbour is closest
				String ipaddressOfNeighbor=computeMin(x,y);
				//forward request to the given ip address
				System.out.println("Routing join request to neighbor "+ipaddressOfNeighbor);
				Socket sock=new Socket(ipaddressOfNeighbor, 8080);
				ObjectOutputStream out1 = new ObjectOutputStream (sock.getOutputStream ());
				out1.writeObject("join");
				out1.writeObject(x);
				out1.writeObject(y);
				out1.writeObject(ipaddressOfClient);
				sock.close();
			}    
		}
	}
	
	/**
	 * 	update current keyword list to transfer selected keywords to neighbor zone
	 *  
	 *  @return keywords of neighbor
	 */
	
	private Set<String> updateKeywordList() {
		Set<String>neighborKeywordSet=new HashSet<String>();
		Vector<String>keywordsToBeDeleted=new Vector<String>();
		for(String keyword: keywordSet) {
			double x=0, y=0;
			char character;
			for (int i = 0; i < keyword.length(); i++) {
				character=keyword.charAt(i);
				if(i%2!=0) {
					x+=((double)character)%10;
				}
				else {
					y=y+((double)character)%10;	
				}
				x=x%10;
				y=y%10;
			}
			//check if coordinates are in the region
			if((x>=currentCoordinate.lx&&x<=currentCoordinate.hx)&&(y>=currentCoordinate.ly&&y<=currentCoordinate.hy)) {   
				break;
			}
			else {
				keywordsToBeDeleted.add(keyword);
				neighborKeywordSet.add(keyword);
			}
		}
		for(String keyword: keywordsToBeDeleted) {   //to avoid concurrent modification exception
			keywordSet.remove(keyword);
		}
		return neighborKeywordSet;
	}

	/**
	 * 	Splitting current zone when a new node joins
	 *  
	 *  @return neighbor list of new node
	 */
	
	private Map<String, Coordinate> splitzone(Coordinate neighborCoordinate, String ipaddressOfClient) throws UnknownHostException, IOException {
		double width=currentCoordinate.hx-currentCoordinate.lx;
		double height=currentCoordinate.hy-currentCoordinate.ly;
		Map<String, Coordinate> neighborMapofJoiningNode=new HashMap<String, Coordinate> ();
		Vector<String> neighborDeletionListIp=new Vector<String>();
		Vector<String> neighborsTobeNotifiedforDeletion=new Vector<String>();
		if(width==height) {
			neighborCoordinate.lx=currentCoordinate.lx+(currentCoordinate.hx-currentCoordinate.lx)/2;
			neighborCoordinate.ly=currentCoordinate.ly;
			neighborCoordinate.hx=currentCoordinate.hx;
			neighborCoordinate.hy=currentCoordinate.hy;
			for(Map.Entry<String, Coordinate> entry: neighborMap.entrySet()) {
			//return right side's neighbor's ip address and coordinates to the new neighbor
				Coordinate neighbor=entry.getValue();
				//check right
				if(currentCoordinate.hx==neighbor.lx) {  
					neighborDeletionListIp.add(entry.getKey()); //maintain a list of nodes to be deleted
					neighborMapofJoiningNode.put(entry.getKey(), entry.getValue()); 
					neighborsTobeNotifiedforDeletion.add(entry.getKey());
				}
				//check top right
				else if(currentCoordinate.hy==neighbor.ly&&neighbor.lx>=(currentCoordinate.lx+((currentCoordinate.hx-currentCoordinate.lx)/2))) {
					neighborDeletionListIp.add(entry.getKey());
					neighborMapofJoiningNode.put(entry.getKey(), entry.getValue());
					neighborsTobeNotifiedforDeletion.add(entry.getKey());				//inform neighbor to delete node
				}
				//check bottom right
				else if(currentCoordinate.ly==neighbor.hy&&neighbor.lx>=(currentCoordinate.lx+((currentCoordinate.hx-currentCoordinate.lx)/2))) {
					neighborDeletionListIp.add(entry.getKey());
					neighborMapofJoiningNode.put(entry.getKey(), entry.getValue());
					neighborsTobeNotifiedforDeletion.add(entry.getKey());				//inform neighbor to delete node
				}
				//check for top both - add it to both the nodes
				else if(currentCoordinate.hy==neighbor.ly&&neighbor.lx<(currentCoordinate.lx+((currentCoordinate.hx-currentCoordinate.lx)/2))
						&&neighbor.hx>(currentCoordinate.lx+((currentCoordinate.hx-currentCoordinate.lx)/2))) {
					neighborMapofJoiningNode.put(entry.getKey(), entry.getValue());
				}
				//check for bottom both- add it to both the nodes
				else if(currentCoordinate.ly==neighbor.hy&&neighbor.lx<(currentCoordinate.lx+((currentCoordinate.hx-currentCoordinate.lx)/2))
						&&neighbor.hx>(currentCoordinate.lx+((currentCoordinate.hx-currentCoordinate.lx)/2))) {
					neighborMapofJoiningNode.put(entry.getKey(), entry.getValue());
				}
			}
			currentCoordinate.hx=(currentCoordinate.lx+((currentCoordinate.hx-currentCoordinate.lx)/2));
			neighborMapofJoiningNode.put(currentIp, currentCoordinate); //add itself to joining node's neighbor list
			neighborMap.put(ipaddressOfClient, neighborCoordinate);   //add client peer as a neighbor to itself
		}
		else {
			neighborCoordinate.lx=currentCoordinate.lx;
			neighborCoordinate.ly=currentCoordinate.ly+((currentCoordinate.hy-currentCoordinate.ly)/2); /////////
			neighborCoordinate.hx=currentCoordinate.hx;
			neighborCoordinate.hy=currentCoordinate.hy;
			for(Map.Entry<String, Coordinate> entry: neighborMap.entrySet()) {
				//check top
				Coordinate neighbor=entry.getValue();
				if(currentCoordinate.hy==neighbor.ly) {  
					neighborDeletionListIp.add(entry.getKey());    
					neighborMapofJoiningNode.put(entry.getKey(), entry.getValue()); 
					neighborsTobeNotifiedforDeletion.add(entry.getKey());
				}
				//check for left top
				if(currentCoordinate.lx==neighbor.hx&&neighbor.ly>=(currentCoordinate.ly+((currentCoordinate.hy-currentCoordinate.ly)/2))) {
					neighborDeletionListIp.add(entry.getKey());
					neighborMapofJoiningNode.put(entry.getKey(), entry.getValue());
					//inform neighbor to delete node
					neighborsTobeNotifiedforDeletion.add(entry.getKey());
				}
				//check for left both
				if(currentCoordinate.lx==neighbor.hx&&neighbor.ly<(currentCoordinate.ly+((currentCoordinate.hy-currentCoordinate.ly)/2))
						&&neighbor.hy>(currentCoordinate.ly+((currentCoordinate.hy-currentCoordinate.ly)/2))) {
					neighborMapofJoiningNode.put(entry.getKey(), entry.getValue());
				}
				//check for right top
				if(currentCoordinate.hx==neighbor.lx&&neighbor.ly>=(currentCoordinate.ly+((currentCoordinate.hy-currentCoordinate.ly)/2))) {
					neighborDeletionListIp.add(entry.getKey());
					neighborMapofJoiningNode.put(entry.getKey(), entry.getValue());
					//inform neighbor to delete node
					neighborsTobeNotifiedforDeletion.add(entry.getKey());
				}
				//check for right both
				if(currentCoordinate.hx==neighbor.lx&&neighbor.hy>(currentCoordinate.ly+((currentCoordinate.hy-currentCoordinate.ly)/2))
						&&neighbor.ly<(currentCoordinate.ly+((currentCoordinate.hy-currentCoordinate.ly)/2))) {
					neighborMapofJoiningNode.put(entry.getKey(), entry.getValue());
				}
			}
			currentCoordinate.hy=(currentCoordinate.ly+((currentCoordinate.hy-currentCoordinate.ly)/2));
			neighborMapofJoiningNode.put(currentIp, currentCoordinate); //add itself to joining node's neighbor list
			neighborMap.put(ipaddressOfClient, neighborCoordinate);//add client peer as a neighbor to itself
		}
		//update all neighboring nodes with new coordinates
		for(String ipaddressOfNeighbor: neighborMap.keySet()) {
			String deleteCurrentNode="false";
			if(neighborsTobeNotifiedforDeletion.contains(ipaddressOfNeighbor)) {
				System.out.println("informing ip "+ipaddressOfNeighbor+" to delete me");
				deleteCurrentNode="true";
			}
			Socket neighborSocket=new Socket(ipaddressOfNeighbor,8080);
			ObjectOutputStream out = new ObjectOutputStream (neighborSocket.getOutputStream ());
			out.writeObject("update");
			out.writeObject(currentCoordinate); //sending coordinates of node to all its neighbors 
			out.writeObject(currentIp);
			out.writeObject(deleteCurrentNode);
			neighborSocket.close();
		}
		//update all neighboring nodes of new node with new node's coordinates
		for(String ipaddressOfNewPeersNeighbor: neighborMapofJoiningNode.keySet()) {
			String deleteCurrentNode="false";
			Socket neighborSocket=new Socket(ipaddressOfNewPeersNeighbor,8080);
			ObjectOutputStream out = new ObjectOutputStream (neighborSocket.getOutputStream ());
			out.writeObject("update");
			out.writeObject(neighborCoordinate); //sending coordinates of node to all its neighbors 
			out.writeObject(ipaddressOfClient);
			out.writeObject(deleteCurrentNode);
			neighborSocket.close();
		}
		//delete unneeded nodes from current neighbor list
		for(String ipOfNeighborTobeDeleted: neighborDeletionListIp) {
			neighborMap.remove(ipOfNeighborTobeDeleted); //remove right side node as neighbor to current node
		}
		//updateNeighbors(n, neighborMapofJoiningNode);
		return neighborMapofJoiningNode;
	}

	/**
	 * 	Compute minimum neighbor to route data
	 *  
	 *  @return ip address of neighbor closest to target
	 */
	
	private String computeMin(double x, double y) {
		double min=1000000000;
		double minDistToNeighbor;
		String ipaddressOfNeighbor=null;
		//for all neighbours, check which neighbour is closest
		for(Map.Entry<String, Coordinate> entry: neighborMap.entrySet()) {
			minDistToNeighbor=minNeighbor(x,y, entry.getValue());
			if(minDistToNeighbor<min) {
				min=minDistToNeighbor;
				ipaddressOfNeighbor=entry.getKey();
			}
		}
		return ipaddressOfNeighbor;
	}

	/**
	 * 	calculate shortest distance from one zone to target point
	 *  
	 *  @return minimum distance
	 */
	
	private double minNeighbor(double x, double y, Coordinate neighbor) {
		double minArray[]=new double[4];
		double min;
		min=minArray[0]=Math.sqrt(Math.pow(y - neighbor.ly, 2) + Math.pow(x - neighbor.lx, 2));
		minArray[1]=Math.sqrt(Math.pow(y - neighbor.hy, 2) + Math.pow(x - neighbor.lx, 2));
		minArray[2]=Math.sqrt(Math.pow(y - neighbor.ly, 2) + Math.pow(x - neighbor.hx, 2));
		minArray[3]=Math.sqrt(Math.pow(y - neighbor.hy, 2) + Math.pow(x - neighbor.hx, 2));
		for (int i = 0; i < minArray.length; i++) {
			if(min<minArray[i]) {
				min=minArray[i];
			}
		}
		return min;
	}
	
}
