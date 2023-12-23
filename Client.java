import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    protected static final String ADMIN_MESSAGE = "Admin123!@#";
    protected static final String KICKED_MESSAGE = "kicked from chat";
    protected static Socket socket;
    protected static Thread receiveThread;
    protected static Thread sendThread;

    public static void main(String[] args) throws IOException{
        socket = new Socket("localhost", 1234);
        String handshakeMessage = "Regular client connection";
        if (args.length > 0) {
            handshakeMessage = args[0];
        }
        startSendThread(handshakeMessage);
        startReceiveThread();
    }

    protected static void startReceiveThread() {
        receiveThread = new Thread(() -> {
            try (Scanner in = new Scanner(socket.getInputStream())) {
                while (!Thread.currentThread().isInterrupted()) {
                    String serverMessage = in.nextLine();
                    if (serverMessage.equals(ADMIN_MESSAGE + KICKED_MESSAGE)) {
                        System.out.println("You have been kicked from chat");
                        shutdown();
                        break;
                    }
                    System.out.println(serverMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        receiveThread.start();
    }

    protected static void startSendThread(String handshakeMessage) {
        sendThread = new Thread(() -> {
            try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                out.println(handshakeMessage);
                Scanner consoleIn = new Scanner(System.in);
                while (!Thread.currentThread().isInterrupted()) {
                    String message = consoleIn.nextLine();
                    out.println(message);
                    if (message.equals("q")) {
                        shutdown();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        sendThread.start();
    }

    protected static void shutdown() {
        if (receiveThread.isAlive()) {
            receiveThread.interrupt();
        }
        if (sendThread.isAlive()) {
            sendThread.interrupt();
        }
    }

}
