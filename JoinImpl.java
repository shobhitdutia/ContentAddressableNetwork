/* 
 * JoinImpl.java 
 * 
 * Version: 
 *     $Id$ 
 * 
 * Revisions: 
 *     $Log$ 
 */
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

/**
 * This program is the implementation of the interface
 *
 * @author      Shobhit Dutia
 */

public class JoinImpl extends UnicastRemoteObject implements JoinInterface {
	private static final long serialVersionUID = 1L;
	static int countOfPeers=0;
	static Map<Integer, String> ipMap=new HashMap<Integer, String>();
	
	protected JoinImpl() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * join request method.
	 * 
	 * @param    args   	ip address of new node
	 * @return 	 ip address of bootstrap node	
	 */


	@Override
	public String joinRequest(String ipaddress) throws RemoteException {
		String ipaddressOfBootstrapNode=null;
		if(ipMap.isEmpty()) {
			ipMap.put(countOfPeers++, ipaddress);
			System.out.println("1st peer joined!");
			return null;
		}
		else {		
			System.out.println((countOfPeers+1)+" peer joining. IP of peer:"+ipaddress);
			ipMap.put(countOfPeers++, ipaddress); //put the ip address of the requesting node in the bootstrap server 
			ipaddressOfBootstrapNode=ipMap.get(0);
			System.out.println("returning "+ipaddressOfBootstrapNode);
			return ipaddressOfBootstrapNode;
		}
	}
}