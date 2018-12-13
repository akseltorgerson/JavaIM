import javax.swing.JFrame;

public class ServerTest {
    public static void main(String[] args) {
        Server mainServer = new Server();
        mainServer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainServer.start();
    }
}
