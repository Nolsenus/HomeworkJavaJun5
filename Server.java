import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    private static final String ADMIN_MESSAGE = "Admin123!@#";
    private static final String KICKED_MESSAGE = "kicked from chat";
    private static Long nextClientID = 0L;
    private static final HashMap<Long, ClientSocketWrapper> clients = new HashMap<>();

    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(1234);
            while(true) {
                Socket clientSocket = server.accept();
                new Thread(() -> {
                    try (ClientSocketWrapper client = new ClientSocketWrapper(clientSocket)) {
                        long clientID = nextClientID;
                        clients.put(clientID, client);
                        nextClientID++;
                        while (true) {
                            handleMessage(client.receiveMessage(), client.isAdminClient(), clientID);
                        }
                    } catch (IOException e) {
                        System.out.println("Could not establish connection with client with socket" + clientSocket);
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleMessage(String message, boolean isAdminClient, long clientID) {
        if (message.matches("@[0-9]+ .+")) {
            int firstSpace = message.indexOf(' ');
            long receiverID = Long.parseLong(message.substring(1, firstSpace));
            String messageText = message.substring(firstSpace + 1);
            if (clients.containsKey(receiverID)) {
                clients.get(receiverID).sendMessage(clientID + ": " + messageText);
            } else {
                clients.get(clientID).sendMessage("Client with ID " + receiverID + "does not exist.");
            }
        } else if (message.startsWith("kick ") && isAdminClient) {
            String afterKick = message.substring(5);
            if (afterKick.matches("[0-9]+")) {
                Long kickedID = Long.parseLong(afterKick);
                if (clients.containsKey(kickedID)) {
                    if(!clients.get(kickedID).isAdminClient()) {
                        try {
                            clients.get(kickedID).sendMessage(ADMIN_MESSAGE + KICKED_MESSAGE);
                            clients.remove(kickedID);
                        } catch (Exception e) {
                            System.out.println("Could not close connection with client with ID " + kickedID + ".");
                            e.printStackTrace();
                        }
                    } else {
                        clients.get(clientID).sendMessage("Admins cannot kick each other.");
                    }
                } else {
                    clients.get(clientID).sendMessage("Client with ID " + kickedID + "does not exist.");
                }
            } else {
                clients.get(clientID).sendMessage("Kick command must follow pattern \"kick *id*\", " +
                        "where *id* is the ID of client to be kicked.");
            }
        } else if (message.equals("q")) {
            try {
                clients.get(clientID).close();
            } catch (Exception e) {
                System.out.println("Could not close connection with client with ID " + clientID + ".");
                e.printStackTrace();
            }
            clients.remove(clientID);
        } else {
            clients.keySet().stream()
                    .filter(it -> it != clientID)
                    .forEach(it -> clients.get(it).sendMessage(clientID + ": " + message));
        }
    }

}
