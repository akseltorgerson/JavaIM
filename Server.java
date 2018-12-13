import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends JFrame {

    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private ServerSocket server;
    private Socket connection;

    // constructor
    public Server () {
        super("Server"); // calls the JFrame constructor and that is the title
        userText = new JTextField(); // create a JTextField object
        userText.setEditable(false); // cannot edit message box if you're not connected
        userText.addActionListener(
            new ActionListener() {
                @Override public void actionPerformed(ActionEvent event) {
                    sendMessage(event.getActionCommand());
                    userText.setText("");
                }
            }
        );
        add(userText, BorderLayout.SOUTH);
        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow), BorderLayout.CENTER);
        chatWindow.setEditable(false);
        setSize(300, 150);
        setVisible(true);
    }

    //set up and run the server
    public void start() {
        try {
            server = new ServerSocket(8080, 25);
            while(true) {
                try {
                    waitForConnection();
                    setupStreams();
                    whileChatting();
                } catch (EOFException e) {
                    showMessage( "\nServer has ended the connection! ");
                } finally {
                    stop();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // waiting for a connection to be established with a client
    private void waitForConnection() throws IOException {
        showMessage("Waiting to connect with client... \n");
        connection = server.accept();
        showMessage("Connected to: " + connection.getInetAddress().getHostName());
    }

    // setting up the streams to send and receive data
    private void setupStreams() throws IOException {
        // setting up the output stream
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush(); // just clean the output stream so nothing gets left over accidentally

        // setting up the input stream
        input = new ObjectInputStream(connection.getInputStream());
        showMessage("\nCommunication streams now established! \n");
    }

    // while the chat is going on
    private void whileChatting() throws IOException {
        String message = "You are now connected! ";
        sendMessage(message);
        ableToType(true);
        do {
            try {
                message = (String) input.readObject();
                showMessage("\n" + message);
            } catch (ClassNotFoundException e) {
                showMessage("\nERROR: UNKNOWN MESSAGE FROM CLIENT! ");
            }
        } while(!message.equals("CLIENT - END"));
    }

    // close streams and sockets after done chatting
    private void stop() {
        showMessage("\nClosing Connections... \n");
        ableToType(false);
        try {
            output.close();
            input.close();
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        showMessage("\nConnections terminated!\n");
    }

    // sends a message to the client
    private void sendMessage(String message) {
        try {
            output.writeObject("SERVER - " + message);
            output.flush();
            showMessage("\nSERVER - " + message);
        } catch (IOException e) {
            chatWindow.append("\nERROR: CANNOT SEND MESSAGE! ");
        }
    }

    // updates chatWindow
    private void showMessage(final String text) {
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                         chatWindow.append(text);
                }
            }
        );
    }

    // sets the text field to be editable or not
    private void ableToType(final boolean flag) {
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    userText.setEditable(flag);
                }
            }
        );
    }
}
