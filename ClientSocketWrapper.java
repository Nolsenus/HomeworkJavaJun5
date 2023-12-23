import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientSocketWrapper implements AutoCloseable {
    private static final String ADMIN_MESSAGE = "Admin123!@#";
    private final Scanner in;
    private final PrintWriter out;
    private final Socket socket;
    private final boolean isAdminClient;

    public ClientSocketWrapper(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new Scanner(socket.getInputStream());
        this.out = new PrintWriter(socket.getOutputStream(), true);
        String handshakeMessage = receiveMessage();
        this.isAdminClient = handshakeMessage.equals(ADMIN_MESSAGE);
        sendMessage("Connected with" + (isAdminClient? "" : "out") + " admin privileges");
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String receiveMessage() {
        return in.nextLine();
    }

    public boolean isAdminClient() {
        return isAdminClient;
    }

    @Override
    public void close() throws Exception {
        in.close();
        out.close();
        socket.close();
    }
}
