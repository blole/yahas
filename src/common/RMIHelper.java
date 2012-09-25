package common;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class RMIHelper {
	private static final boolean useSecurityManager = false;

	public static void makeSureRegistryIsStarted(int port) {
		try {
			LocateRegistry.createRegistry(port);
			System.out.printf("Registry for port %d is not running.\n", port);
			System.out.printf("Please run 'rmiregistry%s &' before running this.\n", port==1099?"":" "+port);
			System.exit(1);
		} catch (RemoteException e) {
		}
	}

//	public static Registry getRegistry(int port) {
//		System.out.printf("Registry for port %d: ", port);
//		try {
//			Registry reg = LocateRegistry.createRegistry(port);
//			System.out.println("Started");
//			return reg;
//		} catch (RemoteException e) {
//			System.out.println("Already started");
//			try {
//				return LocateRegistry.getRegistry();
//			} catch (RemoteException e1) {
//				System.err.println("WTF?");
//				e1.printStackTrace();
//				System.exit(1);
//				return null;
//			}
//		}
//	}

	public static void maybeStartSecurityManager() {
		if (useSecurityManager) {
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new RMISecurityManager());
				System.out.println("Security manager installed.");
			} else {
				System.out.println("Security manager already exists.");
			}
		}
		else
		{
			System.out.println("Not using a security manager.");
		}
	}
}
