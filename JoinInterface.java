/* 
 * JoinInterface.java 
 * 
 * Version: 
 *     $Id$ 
 * 
 * Revisions: 
 *     $Log$ 
 */
public interface JoinInterface extends java.rmi.Remote {

	/**
	 * join request method.
	 * 
	 * @param    args   	ip address of new node
	 * @return 	 ip address of bootstrap node	
	 */

	String joinRequest(String ipaddress) throws java.rmi.RemoteException;
}
