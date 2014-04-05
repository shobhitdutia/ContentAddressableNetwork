/* 
 * BootStrap.java 
 * 
 * Version: 
 *     $Id$ 
 * 
 * Revisions: 
 *     $Log$ 
 */
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * This program initializes the bootstrap server
 *
 * @author      Shobhit Dutia
 */

public class Bootstrap {

	/**
	 * The main program.
	 * 
	 * @param    args   command line arguments (ignored) 
	 * @exception UnknownHostException 
	 */

	public static void main(String[] args) throws UnknownHostException {
		try {
			JoinInterface obj=new JoinImpl();
			Registry reg1 = LocateRegistry.createRegistry(12459);
			// Binding the object in the rmi registry				
			reg1.rebind("Shobhit_bootstrapObject", obj);
			System.out.println("Bootstrap server bound in registry on port 12459!");
			System.out.println("Please provide bootstrap IP "+InetAddress.getLocalHost().getHostAddress()+" to peers");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
