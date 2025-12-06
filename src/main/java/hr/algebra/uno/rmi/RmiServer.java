package hr.algebra.uno.rmi;

import hr.algebra.uno.network.NetworkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RmiServer {
    private static final Logger log = LoggerFactory.getLogger(RmiServer.class);
    private static final int RANDOM_PORT_HINT = 0;
    public static final int RMI_PORT = 1099;
    public static final String HOSTNAME = "localhost";

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            ChatRemoteService chatRemoteService = new ChatRemoteServiceImpl();
            ChatRemoteService skeleton = (ChatRemoteService) UnicastRemoteObject.exportObject(chatRemoteService,
                    RANDOM_PORT_HINT);
            registry.rebind(ChatRemoteService.REMOTE_OBJECT_NAME, skeleton);
            System.err.println("Object registered in RMI registry");
        } catch (RemoteException e) {
            log.error("Documentation file failed to generate.", e);
        }

    }
}