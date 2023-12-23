import java.io.IOException;

public class AdminClient extends Client {

    public static void main(String[] args) throws IOException {
        Client.main(new String[] {ADMIN_MESSAGE});
    }
}
