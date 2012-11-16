package common;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

public class RMIHelper {
	private static final boolean useSecurityManager = false;
	private static final Logger LOGGER = Logger.getLogger(RMIHelper.class.getCanonicalName());
	
	

	/**
	 * Function will try and create an RMI Registry
	 * If it is able to create, it means, RMI Registry is not run
	 * Hence, we instruct the user to Run the RMI Regsitry.
	 *  @param port: Port to Run RMI Registry
	 */
	public static void makeSureRegistryIsStarted(int port) {
		try {
			LocateRegistry.createRegistry(port);
		LOGGER.error("Opps.. rmiregistry is not running. Please run rmiregistry");
		LOGGER.error("System Exiting !! ");
			System.exit(1);
		} catch (RemoteException e) {
		}
	}

	public static void maybeStartSecurityManager() {
		if (useSecurityManager) {
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new RMISecurityManager());
				System.out.println("Security manager installed.");
			} else {
				System.out.println("Security manager already exists.");
			}
		} else {
			System.out.println("Not using a security manager.");
		}
	}

	public static Remote getStub(Remote remote) throws RemoteException {
		return UnicastRemoteObject.exportObject(remote, 0);
	}

	/**
	 * The same as {@code Naming.lookup(hostAddress)} except that it prints a
	 * nice error message to System.out and returns null instead of casting
	 * exceptions.
	 * 
	 * @param hostAddress
	 *            example: {@code //127.0.0.1/test}
	 * @return the found stub, or null if there was an error
	 */
	public static Remote lookup(String hostAddress) {
		try {
			return Naming.lookup(hostAddress);
		} catch (MalformedURLException e) {
			System.err.println(e.getLocalizedMessage());
		} catch (RemoteException e) {
			System.err.println(e.getLocalizedMessage());
		} catch (NotBoundException e) {
			System.err.printf(
					"rmiregistry is running on the remote host (%s),\n"
							+ "   but '%s' is not bound.\n", hostAddress,
					e.getMessage());
		}
		return null;
	}

	public static Remote lookupAndWaitForRemoteToStartIfNecessary(String name,
			int retryPeriod_ms) {
		int attempts = 0;
		while (true) {
			try {
				return Naming.lookup(name);
			} catch (RemoteException | NotBoundException e) {
				if (attempts == 0) {
					System.err.printf(
							"error while connecting to remote host '%s':\n",
							name);
					if (e instanceof NotBoundException)
						System.err
								.printf("rmiregistry is running, but '%s' is not bound.\n",
										e.getLocalizedMessage());
					else
						System.err.println(e.getMessage());
					System.err.printf("\nretrying every %d seconds\n",
							retryPeriod_ms / 1000);
				} else {
					System.err.printf("retry %d failed...\r", attempts);
				}
				attempts++;

				try {
					Thread.sleep(retryPeriod_ms);
				} catch (InterruptedException e1) {
				}

			} catch (MalformedURLException e) {
				throw new RuntimeException("Error connecting to remote '"+name+"', malformed URL");
			}
		}
	}

	public static void rebindAndHookUnbind(final String string, Remote stub)
			throws RemoteException, MalformedURLException {
		Naming.rebind(string, stub);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					Naming.unbind(string);
				} catch (RemoteException | MalformedURLException
						| NotBoundException e) {
				}
			}
		});
	}
}
